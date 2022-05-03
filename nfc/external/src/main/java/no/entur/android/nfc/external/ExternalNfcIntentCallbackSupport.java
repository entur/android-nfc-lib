package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ExternalNfcIntentCallbackSupport {

	private static final String TAG = ExternalNfcIntentCallbackSupport.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcIntentCallback callback;
	protected final Activity activity;

	public ExternalNfcIntentCallbackSupport(ExternalNfcIntentCallback callback, Activity activity) {
		this.callback = callback;
		this.activity = activity;
	}

	private boolean recieveNfcIntentBroadcasts = false;

	protected boolean enabled = false;

	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			callback.onNfcIntent(intent);
		}
	};

	public void onResume() {
		if (enabled) {
			startReceivingNfcIntentBroadcasts();
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingNfcIntentBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingNfcIntentBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingNfcIntentBroadcasts();
		}
		this.enabled = enabled;
	}

	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		activity.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	private void startReceivingNfcIntentBroadcasts() {
		if (!recieveNfcIntentBroadcasts) {
			Log.d(TAG, "Start receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED);
			filter.addAction(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
			filter.addAction(ExternalNfcTagCallback.ACTION_NDEF_DISCOVERED);

			activity.registerReceiver(intentReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingNfcIntentBroadcasts() {
		if (recieveNfcIntentBroadcasts) {
			Log.d(TAG, "Stop receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = false;

			activity.unregisterReceiver(intentReceiver);
		}
	}

}
