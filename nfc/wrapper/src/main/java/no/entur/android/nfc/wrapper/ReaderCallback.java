package no.entur.android.nfc.wrapper;

import android.content.Intent;

public interface ReaderCallback {

	/**
	 *
	 * Reader callback.
	 *
	 * @param tag tag discovered
	 * @param intent can be null
	 */

	void onTagDiscovered(Tag tag, Intent intent);
}
