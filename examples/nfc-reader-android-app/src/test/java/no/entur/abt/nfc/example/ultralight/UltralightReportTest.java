package no.entur.abt.nfc.example.ultralight;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class UltralightReportTest {

	private static final byte[] UID = hex("04E1412A3B5C80");
	private static final byte[] NTAG213_VERSION = hex("0004040201000F03");

	@Test
	public void testFamilyFromVersion() {
		assertEquals(TagFamily.NTAG210, TagFamily.fromVersion(hex("0004040101000B03")));
		assertEquals(TagFamily.NTAG212, TagFamily.fromVersion(hex("0004040101000E03")));
		assertEquals(TagFamily.NTAG213, TagFamily.fromVersion(NTAG213_VERSION));
		assertEquals(TagFamily.NTAG215, TagFamily.fromVersion(hex("0004040201001103")));
		assertEquals(TagFamily.NTAG216, TagFamily.fromVersion(hex("0004040201001303")));
		assertEquals(TagFamily.ULTRALIGHT_EV1_MF0UL11, TagFamily.fromVersion(hex("0004030101000B03")));
		assertEquals(TagFamily.ULTRALIGHT_EV1_MF0UL21, TagFamily.fromVersion(hex("0004030101000E03")));
		// Ultralight EV1 50 pF (MF0ULH11 / MF0ULH21)
		assertEquals(TagFamily.ULTRALIGHT_EV1_MF0UL11, TagFamily.fromVersion(hex("0004030201000B03")));
		assertEquals(TagFamily.ULTRALIGHT_EV1_MF0UL21, TagFamily.fromVersion(hex("0004030201000E03")));
		// other products with the same storage size: NTAG I2C 1k (version 2), NTAG216F (subtype 04), NTAG213 with another subtype
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0004040502001303")));
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0004040401001303")));
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0004040101000F03")));
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0004030301000B03")));
		// unknown product, non-NXP, wrong length
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0004050101000F03")));
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("0005040201000F03")));
		assertEquals(TagFamily.OTHER_GET_VERSION, TagFamily.fromVersion(hex("00040402")));
	}

	@Test
	public void testReaderCapability() {
		// pass-through, verified with this screen on an ACR1222L
		assertEquals(ReaderCapability.ACS_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1222 3S PICC Reader"));
		assertEquals(ReaderCapability.ACS_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1222L 3S CL Reader"));
		// pass-through, not verified with this screen
		assertEquals(ReaderCapability.ACS_UNVERIFIED_RAW, ReaderCapability.fromAcsReaderName("ACS ACR122U PICC Interface"));
		assertEquals(ReaderCapability.ACS_UNVERIFIED_RAW, ReaderCapability.fromAcsReaderName("ACS ACR122"));
		// no pass-through
		assertEquals(ReaderCapability.ACS_NO_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1552 1S CL Reader PICC"));
		assertEquals(ReaderCapability.ACS_NO_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1281 1S Dual Reader"));
		assertEquals(ReaderCapability.ACS_NO_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1252 1S CL Reader PICC"));
		assertEquals(ReaderCapability.ACS_NO_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1255U-J1 PICC"));
		assertEquals(ReaderCapability.ACS_NO_RAW, ReaderCapability.fromAcsReaderName("ACS ACR1283U"));
		assertEquals(ReaderCapability.ACS_UNKNOWN_MODEL, ReaderCapability.fromAcsReaderName(null));
		// no expectation for raw commands when the model is not known
		assertNull(Expectations.expected(Check.GET_VERSION, TagFamily.NTAG213, ReaderCapability.ACS_UNKNOWN_MODEL));
		assertEquals(Boolean.TRUE, Expectations.expected(Check.MEMORY, TagFamily.NTAG213, ReaderCapability.ACS_UNKNOWN_MODEL));
	}

	@Test
	public void testExpectations() {
		// NTAG supports the NTAG commands
		assertEquals(Boolean.TRUE, Expectations.expected(Check.GET_VERSION, TagFamily.NTAG213, ReaderCapability.PHONE));
		assertEquals(Boolean.TRUE, Expectations.expected(Check.NFCA_FAST_READ, TagFamily.NTAG213, ReaderCapability.ACS_RAW));
		// unless the reader does not send them
		assertEquals(Boolean.FALSE, Expectations.expected(Check.GET_VERSION, TagFamily.NTAG213, ReaderCapability.ACS_NO_RAW));
		assertEquals(Boolean.FALSE, Expectations.expected(Check.READ_SIG, TagFamily.NTAG213, ReaderCapability.HID_ATR210));
		// ATR210 NfcA and WebSocket forward commands, no expectation
		assertNull(Expectations.expected(Check.NFCA_GET_VERSION, TagFamily.NTAG213, ReaderCapability.HID_ATR210));
		assertNull(Expectations.expected(Check.GET_VERSION, TagFamily.NTAG213, ReaderCapability.WEBSOCKET));
		// reading always works
		assertEquals(Boolean.TRUE, Expectations.expected(Check.MEMORY, TagFamily.NTAG213, ReaderCapability.HID_ATR210));

		// Ultralight / Ultralight C do not support the NTAG commands
		assertEquals(Boolean.FALSE, Expectations.expected(Check.GET_VERSION, TagFamily.ULTRALIGHT, ReaderCapability.PHONE));
		assertEquals(Boolean.FALSE, Expectations.expected(Check.READ_SIG, TagFamily.ULTRALIGHT_C, ReaderCapability.ACS_RAW));

		// Ultralight C authentication, the phone result identifies the family
		assertNull(Expectations.expected(Check.AUTH1, TagFamily.ULTRALIGHT_C, ReaderCapability.PHONE));
		assertEquals(Boolean.TRUE, Expectations.expected(Check.AUTH1, TagFamily.ULTRALIGHT_C, ReaderCapability.ACS_RAW));
		assertEquals(Boolean.FALSE, Expectations.expected(Check.AUTH1, TagFamily.ULTRALIGHT, ReaderCapability.ACS_UNVERIFIED_RAW));
		assertNull(Expectations.expected(Check.AUTH1, TagFamily.NTAG213, ReaderCapability.ACS_RAW));

		// unknown family
		assertNull(Expectations.expected(Check.GET_VERSION, TagFamily.UNKNOWN, ReaderCapability.ACS_RAW));
	}

	@Test
	public void testCell() {
		assertEquals("ok", Expectations.cell(Outcome.ok(null), Boolean.TRUE, false));
		assertEquals("fail", Expectations.cell(Outcome.failed("NAK"), Boolean.FALSE, false));
		assertEquals("!fail", Expectations.cell(Outcome.failed("NAK"), Boolean.TRUE, false));
		assertEquals("!ok", Expectations.cell(Outcome.ok(null), Boolean.FALSE, false));
		assertEquals("ok*", Expectations.cell(Outcome.ok(null), Boolean.TRUE, true));
		assertEquals("!fail", Expectations.cell(Outcome.failed("NAK"), Boolean.TRUE, true));
		assertEquals("fail?", Expectations.cell(Outcome.failed("NAK"), null, false));
		assertEquals("lost", Expectations.cell(Outcome.lost(null), Boolean.TRUE, false));
		assertEquals("skip", Expectations.cell(Outcome.skipped(null), Boolean.TRUE, false));
		assertEquals("-", Expectations.cell(null, Boolean.TRUE, false));
	}

	@Test
	public void testNtagOnPhoneAndReaders() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		record.set(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true));
		// does not send raw commands, so they fail as expected
		record.set(ntag213(external("acs-2", "ACR1252", ReaderCapability.ACS_NO_RAW), false));

		assertEquals(TagFamily.NTAG213, record.getFamily());
		assertFalse(record.isFamilyProvisional());
		assertEquals(0, ReportRenderer.unexpected(record, record.getPhone()));
		assertEquals(0, ReportRenderer.unexpected(record, record.getExternal("acs-1")));
		assertEquals(0, ReportRenderer.unexpected(record, record.getExternal("acs-2")));
		assertEquals("OK", ReportRenderer.result(record));
	}

	@Test
	public void testRawCommandsFailOnPassThroughReader() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		record.set(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), false));

		// GET_VERSION, READ_SIG, NfcA GET_VERSION and FAST_READ
		assertEquals(4, ReportRenderer.unexpected(record, record.getExternal("acs-1")));
		assertEquals("4 unexpected", ReportRenderer.result(record));
	}

	@Test
	public void testTypeAndMemoryDifferences() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		ScanResult external = ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true);
		external.type = 1;
		external.memory[20] = 0x01;
		record.set(external);

		assertEquals(2, ReportRenderer.unexpected(record, external));

		Map<String, String> readers = new LinkedHashMap<>();
		readers.put("acs-1", "ACR122U");
		String details = new ReportRenderer().details(record, readers);
		assertTrue(details, details.contains("!1 Ultralight"));
		assertTrue(details, details.contains("!fail (page 5)"));
		assertTrue(details, details.contains("getType() differs"));
	}

	@Test
	public void testProvisionalFamily() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true));

		assertEquals(TagFamily.NTAG213, record.getFamily());
		assertTrue(record.isFamilyProvisional());
		assertEquals("scan on phone", ReportRenderer.result(record));
	}

	@Test
	public void testReferenceFamilyOnlyFromPhone() {
		UltralightResults results = new UltralightResults();
		results.add(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true));
		assertNull(results.getReferenceFamily(UID));

		results.add(ntag213(phone(), true));
		assertEquals(TagFamily.NTAG213, results.getReferenceFamily(UID));
		assertEquals(Collections.singletonMap("acs-1", "ACR122U"), results.getReaders());
	}

	@Test
	public void testRender() {
		UltralightResults results = new UltralightResults();
		results.add(ntag213(phone(), true));
		results.add(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true));

		String report = new ReportRenderer().render("Test", results.getRecords(), results.getReaders(), results.getEvents());

		assertTrue(report, report.contains("| # | UID            | Tag     | Phone | ACR122U | getType() | Result |"));
		assertTrue(report, report.contains("| 1 | 04E1412A3B5C80 | NTAG213 | ok    | ok      | C/C       | OK     |"));
	}

	@Test
	public void testNfcCounterMirrorIsNotCompared() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		ScanResult phone = ntag213(phone(), true);
		ScanResult external = ntag213(external("acs-1", "ACR1222L", ReaderCapability.ACS_UNVERIFIED_RAW), true);
		for (ScanResult result : new ScanResult[] { phone, external }) {
			// CFG0: NFC counter mirror at page 16 byte 0, CFG1: NFC counter enabled
			result.memory[0x29 * 4] = (byte) 0x80;
			result.memory[0x29 * 4 + 2] = 0x10;
			result.memory[0x2A * 4] = 0x10;
		}
		// counter as ASCII hex, incremented between the scans
		System.arraycopy("000045".getBytes(), 0, phone.memory, 16 * 4, 6);
		System.arraycopy("000046".getBytes(), 0, external.memory, 16 * 4, 6);
		record.set(phone);
		record.set(external);

		assertTrue(ReportRenderer.mismatches(record, external).isEmpty());

		Map<String, String> readers = new LinkedHashMap<>();
		readers.put("acs-1", "ACR1222L");
		String details = new ReportRenderer().details(record, readers);
		assertTrue(details, details.contains("NFC counter mirror at page 16 byte 0"));

		// other differences are still found
		external.memory[17 * 4 + 3] = 0x01;
		assertEquals(1, ReportRenderer.mismatches(record, external).size());
	}

	@Test
	public void testCounterMirrorOnlyWhenCounterEnabled() {
		ScanResult phone = ntag213(phone(), true);
		phone.memory[0x29 * 4] = (byte) 0x80;
		phone.memory[0x29 * 4 + 2] = 0x10;
		assertNull(NtagMirror.counter(TagFamily.NTAG213, phone));

		phone.memory[0x2A * 4] = 0x10;
		assertEquals("NFC counter mirror at page 16 byte 0", NtagMirror.counter(TagFamily.NTAG213, phone).toString());

		// UID and counter mirror: UID (14), x, counter
		phone.memory[0x29 * 4] = (byte) 0xC0;
		assertEquals("NFC counter mirror at page 19 byte 3", NtagMirror.counter(TagFamily.NTAG213, phone).toString());
	}

	@Test
	public void testNtag203Expectations() {
		// no GET_VERSION, READ_SIG, FAST_READ, not an Ultralight C
		assertEquals(Boolean.FALSE, Expectations.expected(Check.GET_VERSION, TagFamily.NTAG203, ReaderCapability.PHONE));
		assertEquals(Boolean.FALSE, Expectations.expected(Check.NFCA_FAST_READ, TagFamily.NTAG203, ReaderCapability.ACS_RAW));
		assertEquals(Boolean.FALSE, Expectations.expected(Check.AUTH1, TagFamily.NTAG203, ReaderCapability.ACS_RAW));
		assertEquals(42, TagFamily.NTAG203.getReadablePages());
	}

	@Test
	public void testTypeLights() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		record.set(ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true));
		record.set(ntag213(external("acs-2", "ACR1252", ReaderCapability.ACS_NO_RAW), false));

		TypeStatus status = status(Collections.singletonList(record), TagFamily.NTAG213);
		assertEquals(TypeStatus.Light.PASS, status.getPhone());
		assertEquals(TypeStatus.Light.PASS, status.getExternal());
		assertEquals(TypeStatus.Light.PASS, status.getMatch());
		assertEquals(1, status.getTags());

		// all listed types are present, untested ones without lights
		List<TypeStatus> statuses = TypeStatus.compute(Collections.singletonList(record));
		assertEquals(TagFamily.ULTRALIGHT, statuses.get(0).getFamily());
		assertEquals(TypeStatus.Light.NONE, status(Collections.singletonList(record), TagFamily.NTAG203).getPhone());
	}

	@Test
	public void testTypeLightsExternalFailsAndMismatch() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		ScanResult external = ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), false);
		external.type = 1;
		record.set(external);

		TypeStatus status = status(Collections.singletonList(record), TagFamily.NTAG213);
		assertEquals(TypeStatus.Light.PASS, status.getPhone());
		// raw commands fail, but should work
		assertEquals(TypeStatus.Light.FAIL, status.getExternal());
		// type differs
		assertEquals(TypeStatus.Light.FAIL, status.getMatch());
		assertFalse(status.getProblems().isEmpty());
	}

	@Test
	public void testTypeLightsSignatureMismatch() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		ScanResult phone = ntag213(phone(), true);
		phone.set(Check.READ_SIG, Outcome.ok("AA"));
		record.set(phone);
		ScanResult external = ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true);
		external.set(Check.READ_SIG, Outcome.ok("BB"));
		record.set(external);

		TypeStatus status = status(Collections.singletonList(record), TagFamily.NTAG213);
		assertEquals(TypeStatus.Light.PASS, status.getExternal());
		assertEquals(TypeStatus.Light.FAIL, status.getMatch());
	}

	@Test
	public void testTypeLightsLost() {
		TagRecord record = new TagRecord(UltralightResults.key(UID), 1);
		record.set(ntag213(phone(), true));
		ScanResult external = ntag213(external("acs-1", "ACR122U", ReaderCapability.ACS_RAW), true);
		external.lost = true;
		record.set(external);

		TypeStatus status = status(Collections.singletonList(record), TagFamily.NTAG213);
		assertEquals(TypeStatus.Light.INCOMPLETE, status.getExternal());
		assertEquals(TypeStatus.Light.INCOMPLETE, status.getMatch());
	}

	private static TypeStatus status(List<TagRecord> records, TagFamily family) {
		for (TypeStatus status : TypeStatus.compute(records)) {
			if (status.getFamily() == family) {
				return status;
			}
		}
		throw new AssertionError("No status for " + family);
	}

	@Test
	public void testTable() {
		String table = ReportRenderer.table(java.util.Arrays.asList(new String[] { "a", "bb" }, new String[] { "ccc", "d" }));
		assertEquals("| a   | bb |\n| --- | -- |\n| ccc | d  |\n", table);
	}

	private static ScanResult phone() {
		ScanResult result = new ScanResult();
		result.source = ScanResult.Source.PHONE;
		result.readerKey = "phone";
		result.readerLabel = "Phone";
		result.capability = ReaderCapability.PHONE;
		return result;
	}

	private static ScanResult external(String key, String label, ReaderCapability capability) {
		ScanResult result = new ScanResult();
		result.source = ScanResult.Source.EXTERNAL;
		result.readerKey = key;
		result.readerLabel = label;
		result.readerName = "ACS " + label;
		result.capability = capability;
		return result;
	}

	/**
	 * A complete NTAG213 scan.
	 *
	 * @param raw whether raw commands succeeded
	 */

	private static ScanResult ntag213(ScanResult result, boolean raw) {
		result.uid = UID;
		result.type = 2;
		result.pagesRead = 45;
		result.pagesExpected = 45;
		result.memory = new byte[45 * 4];
		System.arraycopy(hex("04E1412C2A3B5C80CD480000E1101200"), 0, result.memory, 0, 16);

		result.set(Check.READ, Outcome.ok(null));
		result.set(Check.UID, Outcome.ok(null));
		result.set(Check.BCC, Outcome.ok(null));
		result.set(Check.MEMORY, Outcome.ok(null));
		Outcome rawOutcome = raw ? Outcome.ok(null) : Outcome.failed("Transceive failed");
		result.set(Check.GET_VERSION, rawOutcome);
		result.set(Check.READ_SIG, rawOutcome);
		result.set(Check.NFCA_GET_VERSION, rawOutcome);
		result.set(Check.NFCA_FAST_READ, rawOutcome);
		if (raw) {
			result.version = NTAG213_VERSION;
			result.family = TagFamily.NTAG213;
		}
		return result;
	}

	private static byte[] hex(String string) {
		return ByteArrayHexStringConverter.hexStringToByteArray(string);
	}
}
