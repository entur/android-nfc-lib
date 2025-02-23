package no.entur.android.nfc.hce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import no.entur.android.nfc.NfcReaderCallbackSupport;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.EmptyTransceiveResponseException;
import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

/**
 *
 * NFC reader callback implementation which handles HCE targets and optionally uses a delegate to handler other targets.
 *
 */

@Deprecated // rather use NfcTargetAnalyzer approach
public abstract class AbstractNfcReaderCallbackHceSupport extends NfcReaderCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNfcReaderCallbackHceSupport.class);

	public interface Listener {

		/**
		 * Handle compatible HCE device scanned
		 *
		 * @param protocol command protocol
		 * @return true if safe to close connection, otherwise false
		 * @throws IOException
		 */

		boolean onProtocol(CommandApduProtocol protocol) throws IOException;

		void onUnexpectedTag(Tag tag);

		void onUnsupportedProtocols();

		void onUnknownApplicationIdentifier();

		void onException(Exception exception);

	}

	protected Listener listener;

	// for HCE only
	protected int transceiveTimeout;

	public AbstractNfcReaderCallbackHceSupport(Activity activity, Listener listener, Bundle readerModeExtras,
											   ReaderCallback delegate, int transceiveTimeout, Executor executor, boolean receiverExported) {
		super(activity, delegate, readerModeExtras, executor, receiverExported);
		this.listener = listener;
		this.transceiveTimeout = transceiveTimeout;
	}

	public boolean canHandle(IsoDep isoDep) {
		// check that not a tag
		IsoDepDeviceHint hint = new IsoDepDeviceHint(isoDep);

		if (hint.isTag()) {
			LOGGER.debug("Device hints indicate a Desfire EV1 card");
		} else {
			if (hint.isHostCardEmulation()) {
				LOGGER.debug("Device hints indicate a Host Card Emulation device");
			} else {
				LOGGER.debug("Device hints unable to indicate a type, historical bytes were '"
						+ ByteArrayHexStringConverter.toHexString(hint.getHistoricalBytes()) + "'");
				// might be EMV etc

				// simple detect
				// must parse historical bytes
				if (hint.getHistoricalBytes().length == 0) {
					// assume HCE
					return true;
				}

				return false;
			}
			return true;
		}
		return false;
	}

	public void onTagDiscovered(Tag tag, Intent intent) {
		IsoDep isoDep = IsoDep.get(tag);
		if (isoDep != null) {
			if (canHandle(isoDep)) {
				onTagDiscovered(isoDep);

				return;
			}
		}
		if (delegate != null) {
			delegate.onTagDiscovered(tag, intent);
		} else {
			listener.onUnexpectedTag(tag);
		}
	}

	protected void onTagDiscovered(IsoDep isoDep) {
		boolean close = true;
		try {
			isoDep.connect();
			if (transceiveTimeout != -1) {
				LOGGER.debug("Set timeout " + transceiveTimeout);
				isoDep.setTimeout(transceiveTimeout);
			}
			// select application
			CommandApduProtocol protocol = buildCommandApduProtocol(isoDep);
			if (protocol != null) {
				close = listener.onProtocol(protocol);
			} else {
				listener.onUnsupportedProtocols();
			}
		} catch (ApplicationIdentifierUnknownException e) {
			LOGGER.warn("Unknown application identifier", e);
			listener.onUnknownApplicationIdentifier();
		} catch (EmptyTransceiveResponseException ioe) {
			listener.onException(ioe);
		} catch (Exception e) {
			listener.onException(e);
		} finally {
			if (close) {
				try {
					isoDep.close();
				} catch (IOException ignored) {
					// ignore
				} catch(SecurityException e) {
					// ignore
				}
			}
		}
	}

	protected abstract CommandApduProtocol buildCommandApduProtocol(IsoDep isoDep) throws IOException;

}
