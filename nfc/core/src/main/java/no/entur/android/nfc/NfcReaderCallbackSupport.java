package no.entur.android.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.Executor;

import no.entur.android.nfc.wrapper.ReaderCallback;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagWrapper;

/**
 *
 * Helper for using NFC in activities (with our own wrapped NFC types).
 *
 */

public class NfcReaderCallbackSupport extends AbstractActivitySupport implements NfcAdapter.ReaderCallback, ReaderCallback {

	private static final String TAG = NfcReaderCallbackSupport.class.getName();

	protected NfcAdapter nfcAdapter;

	protected Activity activity;
	protected ReaderCallback delegate;
	protected Bundle extras;
	protected Executor executor; // non-final for testing

	public NfcReaderCallbackSupport(Activity activity, ReaderCallback delegate, Bundle extras, Executor executor) {
		this.activity = activity;
		this.delegate = delegate;
		this.extras = extras;
		this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
		this.executor = executor;
	}

	public void onTagDiscovered(android.nfc.Tag androidNfcTag) {
		onTagDiscovered(new TagWrapper(androidNfcTag));
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onTagDiscovered(Tag tag) {
		if(executor != null) {
			executor.execute(() -> {
				delegate.onTagDiscovered(tag);
			});
		} else {
			delegate.onTagDiscovered(tag);
		}
	}

	public boolean isNfcEnabled() {
		return nfcAdapter != null && nfcAdapter.isEnabled();
	}

	public void setNfcReaderMode(boolean enabled) {
		setEnabled(enabled);
	}

	@Override
	protected void stopImpl() {
		Log.i(TAG, "Stop NFC reader mode");
		nfcAdapter.disableReaderMode(activity);
	}

	@Override
	protected void startImpl() {
		Log.i(TAG, "Start NFC reader mode");
		nfcAdapter.enableReaderMode(activity, this,
				NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, extras);
	}

	@Override
	protected boolean canStart() {
		return super.canStart() && isNfcEnabled();
	}

	@Override
	protected boolean canStop() {
		return super.canStop() && isNfcEnabled();
	}
}
