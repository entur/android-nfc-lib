package no.entur.android.nfc.external.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;

public abstract class AbstractService extends Service {

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
		LOGGER.debug("Bind for intent " + intent.getAction());

		return new Binder();
	}

	public void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

}
