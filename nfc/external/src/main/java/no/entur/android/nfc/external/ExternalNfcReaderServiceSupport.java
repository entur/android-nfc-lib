package no.entur.android.nfc.external;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

public class ExternalNfcReaderServiceSupport {

	private static final String TAG = ExternalNfcReaderServiceSupport.class.getName();

	protected final ExternalNfcServiceCallback callback;
	protected final Activity activity;
	protected final Class<? extends Service> serviceClass;
	protected final boolean foreground;

	private boolean recieveServiceBroadcasts = false;
	private volatile boolean open = false;

	public ExternalNfcReaderServiceSupport(ExternalNfcServiceCallback callback, Activity activity, Class<? extends Service> serviceClass, boolean foreground) {
		this.callback = callback;
		this.activity = activity;
		this.serviceClass = serviceClass;
		this.foreground = foreground;
	}

	private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (ExternalNfcServiceCallback.ACTION_SERVICE_STARTED.equals(action)) {
				if (!open) {
					open = true;

					Log.d(TAG, "Service started");

					callback.onExternalNfcServiceStarted(intent);
				}
			} else if (ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED.equals(action)) {
				if (open) {
					open = false;

					Log.d(TAG, "Service stopped");

					callback.onExternalNfcServiceStopped(intent);
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

	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		activity.sendBroadcast(intent, "android.permission.NFC");
	}

	public void startService() {
		Intent intent = new Intent();
		intent.setClass(activity, serviceClass);
		activity.startService(intent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && foreground) {
			activity.startForegroundService(intent);
		} else {
			activity.startService(intent);
		}
	}

	public void stopService() {
		Intent intent = new Intent();
		intent.setClass(activity, serviceClass);
		activity.stopService(intent);
	}

	private void startReceivingServiceBroadcasts() {
		if (!recieveServiceBroadcasts) {
			Log.d(TAG, "Start receiving service broadcasts");

			recieveServiceBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
			filter.addAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

			activity.registerReceiver(serviceReceiver, filter, "android.permission.NFC", null);
		}
	}

	private void stopReceivingServiceBroadcasts() {
		if (recieveServiceBroadcasts) {
			Log.d(TAG, "Stop receiving broadcasts");

			recieveServiceBroadcasts = false;

			activity.unregisterReceiver(serviceReceiver);
		}
	}
}
