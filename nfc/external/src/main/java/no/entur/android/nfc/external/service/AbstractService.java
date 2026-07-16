package no.entur.android.nfc.external.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;

public abstract class AbstractService extends AbstractForegroundService {

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

}
