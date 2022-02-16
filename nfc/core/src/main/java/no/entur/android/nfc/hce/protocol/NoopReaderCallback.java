package no.entur.android.nfc.hce.protocol;

import android.util.Log;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;

/**
 *
 * Noop-reader callback for ignoring scanned tags.
 *
 */

public class NoopReaderCallback implements ReaderCallback {

	private static final String TAG = NoopReaderCallback.class.getName();

	@Override
	public void onTagDiscovered(Tag tag) {
		Log.d(TAG, "Ignoring tag");
	}
}
