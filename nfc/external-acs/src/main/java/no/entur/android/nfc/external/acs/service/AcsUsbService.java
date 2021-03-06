package no.entur.android.nfc.external.acs.service;

import android.util.Log;

import org.nfctools.api.TagType;
import org.nfctools.spi.acs.AcsTag;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagServiceSupport;
import no.entur.android.nfc.external.acs.tag.TagUtility;
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
