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

public class ExternalNfcReaderCallbackSupport {

	private static final String TAG = ExternalNfcReaderCallbackSupport.class.getName();

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcReaderCallback callback;
	protected final Activity activity;
	protected Executor executor; // non-final for testing

	private boolean recieveReaderBroadcasts = false;

	private volatile boolean open = false;

	protected boolean enabled = false;

	private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (ExternalNfcReaderCallback.ACTION_READER_OPENED.equals(action)) {
			if (!open) {
				open = true;

				Log.d(TAG, "Reader opened");

				if(executor != null) {
					executor.execute(() -> {
						callback.onExternalNfcReaderOpened(intent);
					});
				} else {
					callback.onExternalNfcReaderOpened(intent);
				}
			}
		} else if (ExternalNfcReaderCallback.ACTION_READER_CLOSED.equals(action)) {
			if (open) {
				open = false;

				Log.d(TAG, "Reader closed");

				if(executor != null) {
					executor.execute(() -> {
						callback.onExternalNfcReaderClosed(intent);
					});
				} else {
					callback.onExternalNfcReaderClosed(intent);
				}
			}
		} else {
			throw new IllegalArgumentException("Unexpected action " + action);
		}
		}

	};

	public ExternalNfcReaderCallbackSupport(ExternalNfcReaderCallback callback, Activity activity, Executor executor) {
		this.callback = callback;
		this.activity = activity;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onResume() {
		if (enabled) {
			startReceivingReaderBroadcasts();

			Log.d(TAG, "Ask for reader status");

			broadcast(ExternalNfcReaderCallback.ACTION_READER_STATUS);
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingReaderBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingReaderBroadcasts();

			broadcast(ExternalNfcReaderCallback.ACTION_READER_STATUS);
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingReaderBroadcasts();
		}
		this.enabled = enabled;
	}

	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		activity.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	private void startReceivingReaderBroadcasts() {
		if (!recieveReaderBroadcasts) {
			Log.d(TAG, "Start receiving reader broadcasts");

			recieveReaderBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);
			filter.addAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

			activity.registerReceiver(readerReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingReaderBroadcasts() {
		if (recieveReaderBroadcasts) {
			Log.d(TAG, "Stop receiving broadcasts");

			recieveReaderBroadcasts = false;

			activity.unregisterReceiver(readerReceiver);
		}
	}

}
