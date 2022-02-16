package no.entur.android.nfc.external.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;

/**
 *
 * Utility for listening for reader status.
 *
 */

public class ExternalNfcReaderStatusSupport {

	private static final String TAG = ExternalNfcReaderStatusSupport.class.getName();

	protected final Service service;
	protected final Listener readerStatusListener;

	protected boolean recieveStatusBroadcasts = false;

	public ExternalNfcReaderStatusSupport(Service service, Listener readerStatusListener) {
		this.service = service;
		this.readerStatusListener = readerStatusListener;
	}

	public static interface Listener {
		void onReaderStatusIntent(Intent intent);
	}

	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			Log.d(TAG, "Broadcast reader status " + action);

			readerStatusListener.onReaderStatusIntent(intent);
		}
	};

	public void onResume() {
		startReceivingStatusBroadcasts();
	}

	public void onPause() {
		stopReceivingStatusBroadcasts();
	}

	protected void startReceivingStatusBroadcasts() {
		synchronized (this) {
			if (!recieveStatusBroadcasts) {
				Log.d(TAG, "Start receiving status broadcasts");

				recieveStatusBroadcasts = true;

				// register receiver
				IntentFilter filter = new IntentFilter();
				filter.addAction(ExternalNfcReaderCallback.ACTION_READER_STATUS);

				service.registerReceiver(statusReceiver, filter, "android.permission.NFC", null);
			}
		}
	}

	protected void stopReceivingStatusBroadcasts() {
		synchronized (this) {
			if (recieveStatusBroadcasts) {
				Log.d(TAG, "Stop receiving status broadcasts");

				recieveStatusBroadcasts = false;

				try {
					service.unregisterReceiver(statusReceiver);
				} catch (IllegalArgumentException e) {
					// ignore
				}
			}
		}
	}

}
