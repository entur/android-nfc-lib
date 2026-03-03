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

import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;

public abstract class AbstractForegroundService extends Service {

	public static final String FOREGROUND_NOTIFICATION = "FOREGROUND_NOTIFICATION";
	public static final String FOREGROUND_NOTIFICATION_ID = "FOREGROUND_NOTIFICATION_ID";
	public static final String FOREGROUND_SERVICE_TYPE = "FOREGROUND_SERVICE_TYPE";

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractForegroundService.class);

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LOGGER.info("Starting " + getClass().getName() + " service");

		handleForegroundService(intent);

		return Service.START_STICKY;
	}

	public void handleForegroundService(Intent intent) {
		if (intent.hasExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION)) {
			Notification notification;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				notification = intent.getParcelableExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION, Notification.class);
			} else {
				notification = intent.getParcelableExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION);
			}
			int id = intent.getIntExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION_ID, 0);
			int serviceType = intent.getIntExtra(AbstractForegroundService.FOREGROUND_SERVICE_TYPE, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);

			ServiceCompat.startForeground(this, id, notification, serviceType);
		}
	}
}
