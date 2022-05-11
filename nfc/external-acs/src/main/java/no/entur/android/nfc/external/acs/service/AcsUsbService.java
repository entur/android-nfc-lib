package no.entur.android.nfc.external.acs.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.nfc.tech.MifareUltralight;
import android.os.AsyncTask;
import android.util.Log;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.nfctools.spi.acs.AcsTag;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.acs.smartcard.RemovedCardException;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.IsoDepAdapter;
import no.entur.android.nfc.external.acs.tag.MifareDesfireTagFactory;
import no.entur.android.nfc.external.acs.tag.MifareUltralightAdapter;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagServiceSupport;
import no.entur.android.nfc.external.acs.tag.NfcAAdapter;
import no.entur.android.nfc.external.acs.tag.PN532NfcAAdapter;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.acs.tag.TechnologyType;
import no.entur.android.nfc.external.service.ExternalUsbNfcServiceSupport;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.MifareDesfireTagServiceSupport;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class AcsUsbService extends AbstractAcsUsbService {

	private static final String TAG = AcsUsbService.class.getName();

	protected MifareDesfireTagServiceSupport mifareDesfireTagServiceSupport;
	protected MifareUltralightTagServiceSupport mifareUltralightTagServiceSupport;

	protected boolean ntag21xUltralights = true;

	@Override
	public void onCreate() {
		super.onCreate();

		this.mifareDesfireTagServiceSupport = new MifareDesfireTagServiceSupport(this, binder, store);
		this.mifareUltralightTagServiceSupport = new MifareUltralightTagServiceSupport(this, binder, store, ntag21xUltralights);
	}

	@Override
	protected void handleTagInitRegularMode(int slotNumber, byte[] atr, TagType tagType) {
		Log.d(TAG, "Handle tag in regular mode");
		AcsTag acsTag = new AcsTag(tagType, atr, reader, slotNumber);
		ACSIsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, slotNumber);

		if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
			mifareUltralightTagServiceSupport.mifareUltralight(slotNumber, atr, tagType, acsTag, wrapper, reader.getReaderName());
		} else if (tagType == TagType.DESFIRE_EV1) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				Log.d(TAG, "Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			mifareDesfireTagServiceSupport.desfire(slotNumber, atr, wrapper, uid);
		} else if (tagType == TagType.ISO_14443_TYPE_B_NO_HISTORICAL_BYTES || tagType == TagType.ISO_14443_TYPE_A_NO_HISTORICAL_BYTES
				|| tagType == TagType.ISO_14443_TYPE_A) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				Log.d(TAG, "Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			mifareDesfireTagServiceSupport.hce(slotNumber, atr, wrapper, uid);
		} else {
			TagUtility.sendTechBroadcast(AcsUsbService.this);
		}
	}


}
