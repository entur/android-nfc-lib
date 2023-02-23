package no.entur.android.nfc.hce.protocol;

import android.content.Intent;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.hce.AbstractNfcReaderCallbackHceSupport;
import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;

/**
 *
 * Noop-reader callback for ignoring scanned tags.
 *
 */

public class NoopReaderCallback implements ReaderCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(NoopReaderCallback.class);

	@Override
	public void onTagDiscovered(Tag tag, Intent intent) {
		LOGGER.debug("Ignoring tag");
	}
}
