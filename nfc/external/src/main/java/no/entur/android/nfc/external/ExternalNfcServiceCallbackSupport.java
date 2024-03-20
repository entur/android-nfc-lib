package no.entur.android.nfc.external;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import no.entur.android.nfc.util.RegisterReceiverUtils;

public class ExternalNfcServiceCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcServiceCallbackSupport.class);

	protected final ExternalNfcServiceCallback callback;
	protected final Context context;

	private boolean recieveServiceBroadcasts = false;
	protected Executor executor; // non-final for testing
	protected boolean enabled = false;

	protected boolean receiverExported;

	private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ExternalNfcServiceCallback.ACTION_SERVICE_STARTED.equals(action)) {
			LOGGER.debug("Service started");
			callback.onExternalNfcServiceStarted(intent);

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalNfcServiceStarted(intent);
				});
			} else {
				callback.onExternalNfcServiceStarted(intent);
			}
		} else if (ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED.equals(action)) {
			LOGGER.debug("Service stopped");
			callback.onExternalNfcServiceStopped(intent);

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalNfcServiceStopped(intent);
				});
			} else {
				callback.onExternalNfcServiceStopped(intent);
			}
		} else {
			throw new IllegalArgumentException("Unexpected action " + action);
		}
		}

	};

	public ExternalNfcServiceCallbackSupport(ExternalNfcServiceCallback callback, Context context, Executor executor, boolean receiverExported) {
		this.callback = callback;
		this.context = context;
		this.executor = executor;
		this.receiverExported = receiverExported;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

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
			RegisterReceiverUtils.registerReceiver(
					context,
					serviceReceiver,
					filter,
					"android.permission.NFC",
					null,
					receiverExported
			);
		}
	}

	private void stopReceivingServiceBroadcasts() {
		if (recieveServiceBroadcasts) {
			LOGGER.debug("Stop receiving service broadcasts");

			recieveServiceBroadcasts = false;

			context.unregisterReceiver(serviceReceiver);
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingServiceBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingServiceBroadcasts();
		}
		this.enabled = enabled;
	}
}
