package no.entur.android.nfc.external.acs.service;

import android.app.Service;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.tech.MifareUltralight;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.LockPage;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.IsoDepAdapter;
import no.entur.android.nfc.external.acs.tag.MifareDesfireTagFactory;
import no.entur.android.nfc.external.acs.tag.MifareUltralightAdapter;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.external.acs.tag.NfcAAdapter;
import no.entur.android.nfc.external.acs.tag.PN532NfcAAdapter;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.acs.tag.TechnologyType;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public abstract class AbstractService extends Service {

	private static final String TAG = AbstractService.class.getName();
	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected TagProxyStore store = new TagProxyStore();
	protected INFcTagBinder binder;

	@Override
	public void onCreate() {
		super.onCreate();

		this.binder = new INFcTagBinder(store);
	}

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

}
