package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ExternalNfcTagLostCallbackSupport {

	private static final String TAG = ExternalNfcTagLostCallbackSupport.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcTagLostCallback callback;
	protected final Activity activity;

	public ExternalNfcTagLostCallbackSupport(ExternalNfcTagLostCallback callback, Activity activity) {
		this.callback = callback;
		this.activity = activity;
	}

	private boolean recieveTagLostBroadcasts = false;

	protected boolean enabled = false;

	private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD)) {
				callback.onExternalTagLost(intent);
			} else {
				Log.d(TAG, "Ignore action " + intent.getAction());
			}
		}
	};

	public void onResume() {
		if (enabled) {
			startReceivingTagLostBroadcasts();
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingTagLostBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingTagLostBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingTagLostBroadcasts();
		}
		this.enabled = enabled;
	}

	private void startReceivingTagLostBroadcasts() {
		if (!recieveTagLostBroadcasts) {
			Log.d(TAG, "Start receiving tag lost broadcasts");

			recieveTagLostBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);

			activity.registerReceiver(tagReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingTagLostBroadcasts() {
		if (recieveTagLostBroadcasts) {
			Log.d(TAG, "Stop receiving tag lost broadcasts");

			recieveTagLostBroadcasts = false;

			activity.unregisterReceiver(tagReceiver);
		}
	}

}
