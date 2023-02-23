package no.entur.android.nfc.external.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;

/**
 *
 * Utility for listening for reader status.
 * This means the current status can be requested using a broadcast,
 * then the service will respond with the latest reader status.
 */

public class ExternalNfcReaderStatusSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcReaderStatusSupport.class);

	protected final Context context;
	protected final ExternalNfcReaderStatusListener readerStatusListener;

	protected boolean recieveStatusBroadcasts = false;

	public ExternalNfcReaderStatusSupport(Service context, ExternalNfcReaderStatusListener readerStatusListener) {
		this.context = context;
		this.readerStatusListener = readerStatusListener;
	}

	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			LOGGER.debug("Reader status intent received");

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
				LOGGER.debug("Start receiving status broadcasts");

				recieveStatusBroadcasts = true;

				// register receiver
				IntentFilter filter = new IntentFilter();
				filter.addAction(ExternalNfcReaderCallback.ACTION_READER_STATUS);

				context.registerReceiver(statusReceiver, filter, "android.permission.NFC", null);
			}
		}
	}

	protected void stopReceivingStatusBroadcasts() {
		synchronized (this) {
			if (recieveStatusBroadcasts) {
				LOGGER.debug("Stop receiving status broadcasts");

				recieveStatusBroadcasts = false;

				try {
					context.unregisterReceiver(statusReceiver);
				} catch (IllegalArgumentException e) {
					// ignore
				}
			}
		}
	}

}
