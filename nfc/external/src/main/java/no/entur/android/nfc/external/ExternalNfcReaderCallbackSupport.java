package no.entur.android.nfc.external;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import no.entur.android.nfc.util.RegisterReceiverUtils;

public class ExternalNfcReaderCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcReaderCallbackSupport.class);

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcReaderCallback callback;
	protected final Context context;
	protected Executor executor; // non-final for testing

	private boolean recieveReaderBroadcasts = false;

	protected boolean enabled = false;

	private final BroadcastReceiver readerReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (ExternalNfcReaderCallback.ACTION_READER_OPENED.equals(action)) {
			LOGGER.info("Reader opened");

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalNfcReaderOpened(intent);
				});
			} else {
				callback.onExternalNfcReaderOpened(intent);
			}
		} else if (ExternalNfcReaderCallback.ACTION_READER_CLOSED.equals(action)) {
			LOGGER.info("Reader closed");

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalNfcReaderClosed(intent);
				});
			} else {
				callback.onExternalNfcReaderClosed(intent);
			}
		} else {
			throw new IllegalArgumentException("Unexpected action " + action);
		}
		}

	};

	public ExternalNfcReaderCallbackSupport(ExternalNfcReaderCallback callback, Context context, Executor executor) {
		this.callback = callback;
		this.context = context;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onResume() {
		if (enabled) {
			startReceivingReaderBroadcasts();

			LOGGER.debug("Ask for reader status");

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
		context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	private void startReceivingReaderBroadcasts() {
		if (!recieveReaderBroadcasts) {
			LOGGER.debug("Start receiving reader broadcasts");

			recieveReaderBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);
			filter.addAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
			RegisterReceiverUtils.registerReceiverNotExported(
					context,
					readerReceiver,
					filter,
					ANDROID_PERMISSION_NFC,
					null);
		}
	}

	private void stopReceivingReaderBroadcasts() {
		if (recieveReaderBroadcasts) {
			LOGGER.debug("Stop receiving reader broadcasts");

			recieveReaderBroadcasts = false;

			context.unregisterReceiver(readerReceiver);
		}
	}

}
