package no.entur.abt.nfc.example.ultralight;

import android.content.Intent;
import android.nfc.TagLostException;

import org.nfctools.mf.ul.ntag.NfcNtag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

/**
 * Runs read-only checks against a MIFARE Ultralight / NTAG tag. Never writes, and never sends PWD_AUTH.
 * <br>
 * Each command is sent in its own connect / close, so that on the phone a NAK (which puts the tag in IDLE state)
 * does not affect the next command, as the phone reactivates the tag on connect. External readers might not
 * reactivate the tag, so commands expected to succeed are sent first.
 * <br>
 * On the phone, a tag which does not answer a command (i.e. an Ultralight and GET_VERSION) is reported as lost, and the Tag
 * object can no longer be used. If the tag is still there, it is discovered again, and the scan continues with the next step
 * (see {@link #resume(Session, Tag, Progress)}).
 */

public class UltralightTagTester {

	private static final Logger LOGGER = LoggerFactory.getLogger(UltralightTagTester.class);

	private static final int TIMEOUT = 1000;

	private static final byte[] GET_VERSION = { 0x60 };
	private static final byte[] READ_SIG = { 0x3C, 0x00 };
	private static final byte[] FAST_READ_0_3 = { 0x3A, 0x00, 0x03 };
	private static final byte[] AUTHENTICATE_PART_1 = { 0x1A, 0x00 };

	public interface Progress {
		void onProgress(String message);
	}

	private interface Command {
		Outcome run() throws IOException;
	}

	private enum Step {
		READ, AUTHENTICATE_FIRST, GET_VERSION, READ_SIG, NFCA_GET_VERSION, NFCA_FAST_READ, AUTHENTICATE, PAGE_16, REST_OF_MEMORY, FINISH
	}

	/**
	 * State of a scan, so that it can continue when the phone discovers the tag again.
	 */

	public static class Session {

		private final ScanResult result;
		private final TagFamily known;

		private MifareUltralight mifareUltralight;
		private NfcA nfcA;

		private byte[] firstPages;
		private String memoryFailure;
		/** the memory failure is because the tag was lost */
		private boolean memoryLost;
		private boolean authenticated;

		private int nextStep;
		/** the step during which the tag was dropped, or null */
		private Step pausedStep;
		private long pausedAt;

		private Session(ScanResult result, TagFamily reference) {
			this.result = result;
			this.known = reference != null && reference.isKnown() ? reference : null;
		}

		public ScanResult getResult() {
			return result;
		}

		/**
		 * @return true if the tag was dropped after a command, and the scan continues when the tag is discovered again
		 */

		public boolean isAwaitingRediscovery() {
			return pausedStep != null;
		}

		public long getPausedAt() {
			return pausedAt;
		}

		private boolean isExternal() {
			return result.source == ScanResult.Source.EXTERNAL;
		}
	}

	/**
	 * @param tag tag
	 * @param intent tag intent, can be null
	 * @param result result with source and reader information set
	 * @param reference the tag family as identified by the phone, or null
	 * @param progress progress listener
	 * @return the scan, which might be awaiting rediscovery of the tag
	 */

	public Session start(Tag tag, Intent intent, ScanResult result, TagFamily reference, Progress progress) {
		Session session = new Session(result, reference);

		result.time = System.currentTimeMillis();
		result.uid = tag.getId();
		result.technologies = technologies(tag);
		if (intent != null && intent.hasExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE)) {
			result.ultralightTypeExtra = intent.getIntExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE, -1);
		}

		session.mifareUltralight = MifareUltralight.get(tag);
		if (session.mifareUltralight == null) {
			result.error = "No MifareUltralight technology";
			result.set(Check.READ, Outcome.failed("no MifareUltralight technology"));
			return session;
		}
		result.type = session.mifareUltralight.getType();
		session.nfcA = NfcA.get(tag);

		run(session, progress);
		return session;
	}

	/**
	 * Continue a scan after the phone discovered the tag again.
	 *
	 * @param session paused scan
	 * @param tag the tag, discovered again
	 * @param progress progress listener
	 */

	public void resume(Session session, Tag tag, Progress progress) {
		ScanResult result = session.result;
		Step paused = session.pausedStep;
		session.pausedStep = null;

		session.mifareUltralight = MifareUltralight.get(tag);
		session.nfcA = NfcA.get(tag);
		result.lost = false;

		if (session.memoryLost) {
			// lost while reading the memory (i.e. after GET_VERSION), continue reading where it stopped
			session.memoryFailure = null;
			session.memoryLost = false;
		}

		// the tag is still there, so it did not answer the command, i.e. a NAK. Unless the command worked, and the tag was lost
		// while reading the memory afterwards.
		String noResponse = "no response, the tag was dropped and discovered again";
		switch (paused) {
		case GET_VERSION:
			failIfNotOk(result, Check.GET_VERSION, noResponse);
			break;
		case READ_SIG:
			failIfNotOk(result, Check.READ_SIG, noResponse);
			break;
		case NFCA_GET_VERSION:
			failIfNotOk(result, Check.NFCA_GET_VERSION, noResponse);
			break;
		case NFCA_FAST_READ:
			failIfNotOk(result, Check.NFCA_FAST_READ, noResponse);
			break;
		case AUTHENTICATE_FIRST:
		case AUTHENTICATE:
			failIfNotOk(result, Check.AUTH1, noResponse);
			break;
		case PAGE_16:
			// there is no page 16
			result.family = TagFamily.ULTRALIGHT;
			break;
		default:
			break;
		}
		run(session, progress);
	}

	private static void failIfNotOk(ScanResult result, Check check, String message) {
		Outcome outcome = result.get(check);
		if (outcome == null || !outcome.isOk()) {
			result.set(check, Outcome.failed(message));
		}
	}

	private void run(Session session, Progress progress) {
		Step[] steps = Step.values();
		try {
			while (session.nextStep < steps.length) {
				Step step = steps[session.nextStep];
				session.nextStep++;

				run(session, step, progress);

				if (session.result.lost && step != Step.FINISH) {
					if (!session.isExternal() && step != Step.READ && step != Step.REST_OF_MEMORY) {
						// the phone drops a tag which does not answer, wait for it to be discovered again
						session.pausedStep = step;
						session.pausedAt = System.currentTimeMillis();
						progress.onProgress("tag dropped after " + step + ", keep it on the phone");
						return;
					}
					// skip to the end
					session.nextStep = steps.length - 1;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Problem testing tag", e);
			session.result.error = describe(e);
		}
	}

	private void run(Session session, Step step, Progress progress) {
		ScanResult result = session.result;
		MifareUltralight mifareUltralight = session.mifareUltralight;
		NfcA nfcA = session.nfcA;
		boolean external = session.isExternal();
		TagFamily known = session.known;

		switch (step) {
		case READ: {
			// memory: always safe to read the first 16 pages
			int initialPages = known != null ? known.getReadablePages() : TagFamily.UNKNOWN.getReadablePages();

			progress.onProgress("reading pages");
			Outcome read = command(result, mifareUltralight, () -> {
				byte[] data = mifareUltralight.readPages(0);
				if (data == null || data.length != 16) {
					return Outcome.failed("response " + hex(data));
				}
				session.firstPages = data;
				result.set(Check.UID, uid(result.uid, data));
				result.set(Check.BCC, bcc(data));
				append(result, data, 4);
				readMemory(session, initialPages);
				return Outcome.ok(null);
			});
			result.set(Check.READ, read);
			if (!read.isOk()) {
				result.set(Check.UID, Outcome.skipped("READ failed"));
				result.set(Check.BCC, Outcome.skipped("READ failed"));
			}
			break;
		}
		case AUTHENTICATE_FIRST: {
			// an external reader might not reactivate the tag after a NAK, so authenticate before any command an Ultralight C NAKs
			if (external && known == TagFamily.ULTRALIGHT_C) {
				progress.onProgress("AUTHENTICATE");
				result.set(Check.AUTH1, authenticate(result, nfcA));
				session.authenticated = true;
			}
			break;
		}
		case GET_VERSION: {
			progress.onProgress("GET_VERSION");
			result.set(Check.GET_VERSION, command(result, mifareUltralight, () -> {
				byte[] response = mifareUltralight.transceive(GET_VERSION);
				Outcome outcome = length(response, 8);
				if (outcome.isOk()) {
					setVersion(result, response);
					// read the rest of the memory while the tag is still active
					if (session.memoryFailure == null && result.family.getReadablePages() > result.pagesRead) {
						progress.onProgress("reading pages");
						readMemory(session, result.family.getReadablePages());
					}
				}
				return outcome;
			}));
			break;
		}
		case READ_SIG: {
			if (skipUnsupported(session, Check.READ_SIG)) {
				break;
			}
			progress.onProgress("READ_SIG");
			result.set(Check.READ_SIG, command(result, mifareUltralight, () -> length(mifareUltralight.transceive(READ_SIG), 32)));
			break;
		}
		case NFCA_GET_VERSION: {
			if (skipUnsupported(session, Check.NFCA_GET_VERSION)) {
				break;
			}
			if (nfcA == null) {
				result.set(Check.NFCA_GET_VERSION, Outcome.failed("no NfcA technology"));
				break;
			}
			progress.onProgress("GET_VERSION (NfcA)");
			result.set(Check.NFCA_GET_VERSION, command(result, nfcA, () -> {
				byte[] response = nfcA.transceive(GET_VERSION);
				Outcome outcome = length(response, 8);
				if (outcome.isOk() && result.version == null) {
					setVersion(result, response);
				}
				return outcome;
			}));
			break;
		}
		case NFCA_FAST_READ: {
			if (skipUnsupported(session, Check.NFCA_FAST_READ)) {
				break;
			}
			if (nfcA == null) {
				result.set(Check.NFCA_FAST_READ, Outcome.failed("no NfcA technology"));
				break;
			}
			progress.onProgress("FAST_READ (NfcA)");
			result.set(Check.NFCA_FAST_READ, command(result, nfcA, () -> {
				byte[] response = nfcA.transceive(FAST_READ_0_3);
				Outcome outcome = length(response, 16);
				if (outcome.isOk() && session.firstPages != null && !Arrays.equals(session.firstPages, response)) {
					return Outcome.failed("differs from READ: " + hex(response));
				}
				return outcome;
			}));
			break;
		}
		case AUTHENTICATE: {
			// Ultralight / Ultralight C / NTAG203: these do not support GET_VERSION
			boolean noVersion = result.version == null && (known == null || known.getGetVersion() != Boolean.TRUE);
			if (session.authenticated || !noVersion) {
				break;
			}
			if (external && known == null && commandsSentToTag(result) && anyRawFailed(result)) {
				result.set(Check.AUTH1, Outcome.skipped("tag might be idle after a failed command on an external reader, scan on the phone first"));
			} else {
				progress.onProgress("AUTHENTICATE");
				result.set(Check.AUTH1, authenticate(result, nfcA));
			}
			break;
		}
		case PAGE_16: {
			if (result.family != null) {
				break;
			}
			Outcome authenticate = result.get(Check.AUTH1);
			if (authenticate != null && authenticate.isOk()) {
				result.family = TagFamily.ULTRALIGHT_C;
			} else if (!external && authenticate != null && authenticate.isRun()) {
				// the phone reliably sends the commands, so this is an Ultralight (16 pages) or NTAG203 (42 pages)
				progress.onProgress("page 16");
				Outcome page16 = command(result, mifareUltralight, () -> {
					byte[] data = mifareUltralight.readPages(16);
					if (data != null && data.length == 16) {
						return Outcome.ok(null);
					}
					return Outcome.failed("response " + hex(data));
				});
				if (page16.isOk()) {
					result.family = TagFamily.NTAG203;
				} else if (page16.getStatus() == Outcome.Status.FAILED) {
					result.family = TagFamily.ULTRALIGHT;
				}
			}
			break;
		}
		case REST_OF_MEMORY: {
			// phone: read the rest of the memory, the tag is reactivated on connect
			TagFamily family = result.family != null ? result.family : known;
			if (!external && family != null && session.memoryFailure == null && family.getReadablePages() > result.pagesRead && !result.lost) {
				progress.onProgress("reading pages");
				Outcome outcome = command(result, mifareUltralight, () -> {
					readMemory(session, family.getReadablePages());
					return Outcome.ok(null);
				});
				if (!outcome.isOk() && session.memoryFailure == null) {
					session.memoryFailure = outcome.toString();
				}
			}
			break;
		}
		case FINISH: {
			TagFamily family = result.family != null ? result.family : known;
			result.pagesExpected = (family != null ? family : TagFamily.UNKNOWN).getReadablePages();
			Outcome read = result.get(Check.READ);
			if (read != null && read.isOk()) {
				if (result.lost && result.pagesRead < result.pagesExpected) {
					result.set(Check.MEMORY, Outcome.lost(session.memoryFailure));
				} else if (result.pagesRead >= result.pagesExpected) {
					result.set(Check.MEMORY, Outcome.ok(null));
				} else {
					result.set(Check.MEMORY, Outcome.failed(session.memoryFailure != null ? session.memoryFailure : "read " + result.pagesRead + " pages"));
				}
			} else {
				result.set(Check.MEMORY, Outcome.skipped("READ failed"));
			}
			break;
		}
		default:
			break;
		}
	}

	/**
	 * On the phone, if the tag does not answer GET_VERSION, it is an Ultralight, Ultralight C or NTAG203, which do not support
	 * READ_SIG, FAST_READ (or GET_VERSION via NfcA) either. Do not send these, as each makes the phone drop the tag.
	 */

	private boolean skipUnsupported(Session session, Check check) {
		ScanResult result = session.result;
		if (session.isExternal() || result.version != null) {
			return false;
		}
		Outcome getVersion = result.get(Check.GET_VERSION);
		if (getVersion != null && getVersion.getStatus() == Outcome.Status.FAILED) {
			result.set(check, Outcome.skipped("no GET_VERSION, so not supported by the tag"));
			return true;
		}
		return false;
	}

	private boolean commandsSentToTag(ScanResult result) {
		return result.capability.getSupport(Check.NFCA_GET_VERSION) != ReaderCapability.RawSupport.NOT_SUPPORTED;
	}

	private boolean anyRawFailed(ScanResult result) {
		for (Check check : Check.values()) {
			Outcome outcome = result.get(check);
			if (check.isRaw() && outcome != null && outcome.getStatus() == Outcome.Status.FAILED) {
				return true;
			}
		}
		return false;
	}

	private Outcome authenticate(ScanResult result, NfcA nfcA) {
		if (nfcA == null) {
			return Outcome.failed("no NfcA technology");
		}
		return command(result, nfcA, () -> {
			byte[] response = nfcA.transceive(AUTHENTICATE_PART_1);
			if (response != null && response.length == 9 && (response[0] & 0xFF) == 0xAF) {
				return Outcome.ok(null);
			}
			return Outcome.failed("response " + hex(response));
		});
	}

	private void setVersion(ScanResult result, byte[] version) {
		result.version = version;
		result.family = TagFamily.fromVersion(version);
	}

	/**
	 * Run a command in its own connect / close.
	 */

	private Outcome command(ScanResult result, TagTechnology technology, Command command) {
		if (result.lost) {
			return Outcome.skipped("tag lost");
		}
		try {
			if (technology.isConnected()) {
				technology.close();
			}
			technology.connect();
			setTimeout(technology);
		} catch (TagLostException e) {
			result.lost = true;
			return Outcome.lost("connect: " + describe(e));
		} catch (IOException | RuntimeException e) {
			return Outcome.failed("connect: " + describe(e));
		}
		try {
			return command.run();
		} catch (TagLostException e) {
			result.lost = true;
			return Outcome.lost(describe(e));
		} catch (IOException | RuntimeException e) {
			return Outcome.failed(describe(e));
		} finally {
			try {
				technology.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private void setTimeout(TagTechnology technology) {
		try {
			if (technology instanceof MifareUltralight) {
				((MifareUltralight) technology).setTimeout(TIMEOUT);
			} else if (technology instanceof NfcA) {
				((NfcA) technology).setTimeout(TIMEOUT);
			}
		} catch (Exception e) {
			LOGGER.debug("Unable to set timeout", e);
		}
	}

	private void readMemory(Session session, int pages) {
		session.memoryFailure = readPages(session.mifareUltralight, session.result, pages);
		session.memoryLost = session.memoryFailure != null && session.result.lost;
	}

	/**
	 * Read pages (4 at a time) until the given number of pages have been read.
	 *
	 * @return null if successful, otherwise the problem
	 */

	private String readPages(MifareUltralight mifareUltralight, ScanResult result, int pages) {
		while (result.pagesRead < pages) {
			int page = result.pagesRead;
			byte[] data;
			try {
				data = mifareUltralight.readPages(page);
			} catch (TagLostException e) {
				result.lost = true;
				return "tag lost at page " + page;
			} catch (IOException | RuntimeException e) {
				return "page " + page + ": " + describe(e);
			}
			if (data == null || data.length != 16) {
				return "page " + page + ": response " + hex(data);
			}
			append(result, data, Math.min(4, pages - page));
		}
		return null;
	}

	private void append(ScanResult result, byte[] data, int pages) {
		byte[] memory = Arrays.copyOf(result.memory, result.memory.length + pages * 4);
		System.arraycopy(data, 0, memory, result.memory.length, pages * 4);
		result.memory = memory;
		result.pagesRead += pages;
	}

	private Outcome uid(byte[] uid, byte[] pages) {
		byte[] fromPages = new byte[7];
		System.arraycopy(pages, 0, fromPages, 0, 3);
		System.arraycopy(pages, 4, fromPages, 3, 4);
		if (Arrays.equals(uid, fromPages)) {
			return Outcome.ok(null);
		}
		return Outcome.failed("tag id " + hex(uid) + ", pages " + hex(fromPages));
	}

	private Outcome bcc(byte[] pages) {
		byte bcc0 = (byte) (0x88 ^ pages[0] ^ pages[1] ^ pages[2]);
		byte bcc1 = (byte) (pages[4] ^ pages[5] ^ pages[6] ^ pages[7]);
		if (bcc0 == pages[3] && bcc1 == pages[8]) {
			return Outcome.ok(null);
		}
		return Outcome.failed("expected BCC0 " + hex(new byte[] { bcc0 }) + " BCC1 " + hex(new byte[] { bcc1 }) + ", got " + hex(new byte[] { pages[3] }) + " "
				+ hex(new byte[] { pages[8] }));
	}

	private Outcome length(byte[] response, int length) {
		if (response != null && response.length == length) {
			return Outcome.ok(hex(response));
		}
		return Outcome.failed("response " + hex(response));
	}

	private static String technologies(Tag tag) {
		StringBuilder builder = new StringBuilder();
		for (String technology : tag.getTechList()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(technology.substring(technology.lastIndexOf('.') + 1));
		}
		return builder.toString();
	}

	private static String hex(byte[] data) {
		if (data == null) {
			return "null";
		}
		if (data.length == 0) {
			return "(empty)";
		}
		return ByteArrayHexStringConverter.toHexString(data);
	}

	private static String describe(Exception e) {
		return e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
	}
}
