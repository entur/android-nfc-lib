package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class ExternalNfcIntentCallbackSupport {

	private static final List<String> DEFAULT_ACTIONS = Arrays.asList(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED, ExternalNfcTagCallback.ACTION_TECH_DISCOVERED, ExternalNfcTagCallback.ACTION_NDEF_DISCOVERED);

	private static final String TAG = ExternalNfcIntentCallbackSupport.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcIntentCallback callback;
	protected final Context context;
	protected final List<String> actions;

	public ExternalNfcIntentCallbackSupport(ExternalNfcIntentCallback callback, Context context) {
		this(DEFAULT_ACTIONS, callback, context);
	}

	public ExternalNfcIntentCallbackSupport(List<String> actions, ExternalNfcIntentCallback callback, Context context) {
		this.callback = callback;
		this.context = context;
		this.actions = actions;
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
		context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	private void startReceivingNfcIntentBroadcasts() {
		if (!recieveNfcIntentBroadcasts) {
			Log.d(TAG, "Start receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			for(String action : actions) {
				filter.addAction(action);
			}

			context.registerReceiver(intentReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingNfcIntentBroadcasts() {
		if (recieveNfcIntentBroadcasts) {
			Log.d(TAG, "Stop receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = false;

			context.unregisterReceiver(intentReceiver);
		}
	}

}
