package no.entur.android.nfc.external.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.ServiceCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;

public abstract class AbstractService extends Service {

	public static final String FOREGROUND_NOTIFICATION = "FOREGROUND_NOTIFICATION";
	public static final String FOREGROUND_NOTIFICATION_ID = "FOREGROUND_NOTIFICATION_ID";
	public static final String FOREGROUND_SERVICE_TYPE = "FOREGROUND_SERVICE_TYPE";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected DefaultTagProxyStore store = new DefaultTagProxyStore();
	protected INFcTagBinder binder;


	@Override
	public void onCreate() {
		super.onCreate();

		this.binder = new INFcTagBinder(store);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	public void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LOGGER.info("Starting " + getClass().getName() + " service");

		handleForegroundService(intent);

		return Service.START_STICKY;
	}

	public void handleForegroundService(Intent intent) {
		if (intent.hasExtra(AbstractService.FOREGROUND_NOTIFICATION)) {
			Notification notification;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				notification = intent.getParcelableExtra(AbstractService.FOREGROUND_NOTIFICATION, Notification.class);
			} else {
				notification = intent.getParcelableExtra(AbstractService.FOREGROUND_NOTIFICATION);
			}
			int id = intent.getIntExtra(AbstractService.FOREGROUND_NOTIFICATION_ID, 0);
			int serviceType = intent.getIntExtra(AbstractService.FOREGROUND_SERVICE_TYPE, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);

			ServiceCompat.startForeground(this, id, notification, serviceType);
		}
	}
}
