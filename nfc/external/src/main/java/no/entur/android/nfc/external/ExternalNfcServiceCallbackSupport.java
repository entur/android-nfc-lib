package no.entur.android.nfc.external;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class ExternalNfcServiceCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcServiceCallbackSupport.class);

	protected final ExternalNfcServiceCallback callback;
	protected final Context context;

	private boolean recieveServiceBroadcasts = false;
	private volatile boolean open = false;
	protected Executor executor; // non-final for testing

	public ExternalNfcServiceCallbackSupport(ExternalNfcServiceCallback callback, Context context, Executor executor) {
		this.callback = callback;
		this.context = context;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ExternalNfcServiceCallback.ACTION_SERVICE_STARTED.equals(action)) {
			if (!open) {
				open = true;

				LOGGER.debug("Service started");
				callback.onExternalNfcServiceStarted(intent);

				if(executor != null) {
					executor.execute(() -> {
						callback.onExternalNfcServiceStarted(intent);
					});
				} else {
					callback.onExternalNfcServiceStarted(intent);
				}
			}
		} else if (ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED.equals(action)) {
			if (open) {
				open = false;

				LOGGER.debug("Service stopped");
				callback.onExternalNfcServiceStopped(intent);

				if(executor != null) {
					executor.execute(() -> {
						callback.onExternalNfcServiceStopped(intent);
					});
				} else {
					callback.onExternalNfcServiceStopped(intent);
				}
			}
		} else {
			throw new IllegalArgumentException("Unexpected action " + action);
		}
		}

	};

	public void onResume() {
		startReceivingServiceBroadcasts();
	}

	public void onPause() {
		stopReceivingServiceBroadcasts();
	}

	private void startReceivingServiceBroadcasts() {
		if (!recieveServiceBroadcasts) {
			LOGGER.debug("Start receiving service broadcasts");

			recieveServiceBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
			filter.addAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

			context.registerReceiver(serviceReceiver, filter, "android.permission.NFC", null);
		}
	}

	private void stopReceivingServiceBroadcasts() {
		if (recieveServiceBroadcasts) {
			LOGGER.debug("Stop receiving broadcasts");

			recieveServiceBroadcasts = false;

			context.unregisterReceiver(serviceReceiver);
		}
	}
}
