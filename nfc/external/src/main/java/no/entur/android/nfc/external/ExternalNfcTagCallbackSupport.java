package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

import java.util.concurrent.Executor;

import no.entur.android.nfc.wrapper.Tag;

public class ExternalNfcTagCallbackSupport {

	private static final String TAG = ExternalNfcTagCallbackSupport.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcTagCallback callback;
	protected final Activity activity;

	private boolean recieveTagBroadcasts = false;

	protected boolean enabled = false;
	protected Executor executor; // non-final for testing

	private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalTagDiscovered(tag);
				});
			} else {
				callback.onExternalTagDiscovered(tag);
			}

		} else {
			Log.d(TAG, "Ignore action " + intent.getAction());
		}
		}
	};

	public ExternalNfcTagCallbackSupport(ExternalNfcTagCallback callback, Activity activity, Executor executor) {
		this.callback = callback;
		this.activity = activity;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onResume() {
		if (enabled) {
			startReceivingTagBroadcasts();
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingTagBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingTagBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingTagBroadcasts();
		}
		this.enabled = enabled;
	}

	private void startReceivingTagBroadcasts() {
		if (!recieveTagBroadcasts) {
			Log.d(TAG, "Start receiving tag broadcasts");

			recieveTagBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED);

			activity.registerReceiver(tagReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingTagBroadcasts() {
		if (recieveTagBroadcasts) {
			Log.d(TAG, "Stop receiving tag broadcasts");

			recieveTagBroadcasts = false;

			activity.unregisterReceiver(tagReceiver);
		}
	}

}
