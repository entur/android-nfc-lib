package no.entur.android.nfc.external.acs.service;

import org.nfctools.api.TagType;
import org.nfctools.spi.acs.AcsTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagServiceSupport;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.MifareDesfireTagServiceSupport;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class AcsUsbService extends AbstractAcsUsbService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcsUsbService.class);

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
		LOGGER.debug("Handle tag in regular mode");
		AcsTag acsTag = new AcsTag(tagType, atr, reader, slotNumber);
		ACSIsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, slotNumber);

		if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
			mifareUltralightTagServiceSupport.mifareUltralight(slotNumber, atr, tagType, acsTag, wrapper, reader.getReaderName());
		} else if (tagType == TagType.DESFIRE_EV1) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			mifareDesfireTagServiceSupport.desfire(slotNumber, atr, wrapper, uid, IntentEnricher.identity());
		} else if (tagType == TagType.ISO_DEP) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			mifareDesfireTagServiceSupport.desfire(slotNumber, atr, wrapper, uid, IntentEnricher.identity());
		} else if (tagType == TagType.ISO_14443_TYPE_A) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			mifareDesfireTagServiceSupport.hce(slotNumber, atr, wrapper, uid, IntentEnricher.identity());
		} else {
			TagUtility.sendTechBroadcast(AcsUsbService.this);
		}
	}


}
