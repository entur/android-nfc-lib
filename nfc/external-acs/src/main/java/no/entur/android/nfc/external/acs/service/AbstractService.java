package no.entur.android.nfc.external.acs.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import no.entur.android.nfc.external.acs.tag.MifareUltralightTagServiceSupport;
import no.entur.android.nfc.external.tag.MifareDesfireTagServiceSupport;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;

public abstract class AbstractService extends Service {

	private static final String TAG = AbstractService.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected MifareDesfireTagServiceSupport mifareDesfireTagServiceSupport;
	protected MifareUltralightTagServiceSupport mifareUltralightTagServiceSupport;

	protected TagProxyStore store = new TagProxyStore();
	protected INFcTagBinder binder;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Bind for intent " + intent.getAction());

		return new Binder();
	}

	public void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		this.binder = new INFcTagBinder(store);
		this.mifareDesfireTagServiceSupport = new MifareDesfireTagServiceSupport(this, binder, store);
		this.mifareUltralightTagServiceSupport = new MifareUltralightTagServiceSupport(this, binder, store, true);
	}

}
