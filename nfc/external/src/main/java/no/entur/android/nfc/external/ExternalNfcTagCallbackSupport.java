package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

import no.entur.android.nfc.wrapper.Tag;

public class ExternalNfcTagCallbackSupport {

	private static final String TAG = ExternalNfcTagCallbackSupport.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcTagCallback callback;
	protected final Activity activity;

	public ExternalNfcTagCallbackSupport(ExternalNfcTagCallback callback, Activity activity) {
		this.callback = callback;
		this.activity = activity;
	}

	private boolean recieveTagBroadcasts = false;

	protected boolean enabled = false;

	private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			callback.onExternalTagDiscovered(tag);
		} else {
			Log.d(TAG, "Ignore action " + intent.getAction());
		}
		}
	};

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

	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		activity.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
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
