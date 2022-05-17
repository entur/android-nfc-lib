package no.entur.android.nfc.external;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ExternalNfcServiceAdapter {

	private static final String TAG = ExternalNfcServiceAdapter.class.getName();

	protected final Context context;
	protected final Class<? extends Service> serviceClass;
	protected final boolean foreground;

	public ExternalNfcServiceAdapter(Context activity, Class<? extends Service> serviceClass, boolean foreground) {
		this.context = activity;
		this.serviceClass = serviceClass;
		this.foreground = foreground;
	}

	public void startService() {
		Intent intent = new Intent();
		intent.setClass(context, serviceClass);
		context.startService(intent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && foreground) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

	public void stopService() {
		Intent intent = new Intent();
		intent.setClass(context, serviceClass);
		context.stopService(intent);
	}

}
