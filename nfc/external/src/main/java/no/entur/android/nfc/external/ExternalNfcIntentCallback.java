package no.entur.android.nfc.external;

import android.content.Intent;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;

public interface ExternalNfcIntentCallback {

	void onNfcIntent(Intent intent);
}
