package no.entur.android.nfc.hce.activity;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import java.lang.ref.SoftReference;

import no.entur.android.nfc.NfcReaderCallbackSupport;
import no.entur.android.nfc.NfcReaderCallbackSupportBuilder;
import no.entur.android.nfc.hce.protocol.NoopReaderCallback;

/**
 *
 * Abstract activity for using NFC (as the initiator). Can also be used to 'capture' the NFC device, so that no other interaction is performed.
 *
 */

public abstract class NfcInitiatorCompatActivity extends NfcTargetCompatActivity {

	protected Handler handler = new Handler();
	protected NfcReaderCallbackSupport nfcReaderSupport;

	public static class DisableNfcReaderMode implements Runnable {

		private SoftReference<NfcInitiatorCompatActivity> activity;

		public DisableNfcReaderMode(NfcInitiatorCompatActivity activity) {
			this.activity = new SoftReference<>(activity);
		}

		public void run() {
			NfcInitiatorCompatActivity nfcInitiatorCompatActivity = activity.get();
			if (nfcInitiatorCompatActivity != null) {
				if (!nfcInitiatorCompatActivity.isDestroyed() && !nfcInitiatorCompatActivity.isFinishing()) {
					nfcInitiatorCompatActivity.setNfcReaderMode(false);
				}
			}
		}

	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.nfcReaderSupport = createNfcReaderCallbackSupport();
	}

	// override this method to create a real callback
	protected NfcReaderCallbackSupport createNfcReaderCallbackSupport() {
		NoopReaderCallback noopReaderCallback = new NoopReaderCallback();
		return new NfcReaderCallbackSupportBuilder().withActivity(this).withReaderCallbackDelegate(noopReaderCallback).build();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (nfcReaderSupport != null) {
			nfcReaderSupport.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (nfcReaderSupport != null) {
			nfcReaderSupport.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		handler.removeCallbacksAndMessages(null); // i.e. all
	}

	/**
	 * Enable, then disable reader callbacks. This will effectively block incoming HCE commands.
	 *
	 * @param delay delay in milliseconds
	 */

	public void disableNfcReaderModeWithDelay(long delay) {
		if (nfcReaderSupport != null) {
			nfcReaderSupport.setNfcReaderMode(true);

			handler.postDelayed(new DisableNfcReaderMode(this), delay);
		}
	}

	/**
	 * Enable or disable reader callbacks. This will cancel out any delayed disables
	 *
	 * @param enabled true if enabled
	 */

	public void setNfcReaderMode(boolean enabled) {
		handler.removeCallbacksAndMessages(null); // i.e. all

		nfcReaderSupport.setNfcReaderMode(enabled);
	}

	public boolean isNfcEnabled() {
		return nfcReaderSupport != null && nfcReaderSupport.isNfcEnabled();
	}
}
