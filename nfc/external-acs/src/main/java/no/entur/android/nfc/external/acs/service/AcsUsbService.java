package no.entur.android.nfc.external.acs.service;

import android.content.Intent;

import org.nfctools.api.TagType;
import org.nfctools.spi.acs.AcsTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.MifareUltralightTagServiceSupport;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class AcsUsbService extends AbstractAcsUsbService {

	/**
	 *
	 * Pass this extra to indicate that all mifare ultralight tags are of the NTAG family.
	 *
	 */
	public static final String EXTRA_NTAG_21X_ULTRALIGHTS = ExternalNfcReaderCallback.class.getName() + ".extra.NTAG_21X_ULTRALIGHTS";

	private static final Logger LOGGER = LoggerFactory.getLogger(AcsUsbService.class);

	protected IsoDepTagServiceSupport isoDepTagServiceSupport;
	protected MifareUltralightTagServiceSupport mifareUltralightTagServiceSupport;

	@Override
	public void onCreate() {
		super.onCreate();

		this.isoDepTagServiceSupport = new IsoDepTagServiceSupport(this, binder, store);
		this.mifareUltralightTagServiceSupport = new MifareUltralightTagServiceSupport(this, binder, store, false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean ntags = intent.getBooleanExtra(EXTRA_NTAG_21X_ULTRALIGHTS, false);

		mifareUltralightTagServiceSupport.setNtag21xUltralights(ntags);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void handleTagInitRegularMode(int slotNumber, byte[] atr, TagType tagType) {
		LOGGER.debug("Handle tag in regular mode");
		AcsTag acsTag = new AcsTag(tagType, atr, reader, slotNumber);
		ACSIsoDepWrapper wrapper = new ACSIsoDepWrapper(reader, slotNumber);

		byte[] historicalBytes = TechnologyType.getHistoricalBytes(atr);

		if (tagType == TagType.MIFARE_ULTRALIGHT || tagType == TagType.MIFARE_ULTRALIGHT_C) {
			mifareUltralightTagServiceSupport.mifareUltralight(slotNumber, atr, tagType, acsTag, wrapper, reader.getReaderName());
		} else if (tagType == TagType.DESFIRE_EV1) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			isoDepTagServiceSupport.card(slotNumber, wrapper, uid, historicalBytes, IntentEnricher.identity());
		} else if (tagType == TagType.ISO_DEP) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			isoDepTagServiceSupport.card(slotNumber, wrapper, uid, historicalBytes, IntentEnricher.identity());
		} else if (tagType == TagType.ISO_14443_TYPE_A) {
			byte[] uid = TagUtility.getPcscUid(wrapper);
			if (uid != null) {
				LOGGER.debug("Read tag UID " + ByteArrayHexStringConverter.toHexString(uid));
			}

			isoDepTagServiceSupport.hce(slotNumber, wrapper, uid, historicalBytes, IntentEnricher.identity());
		} else {
			TagUtility.sendTechBroadcast(AcsUsbService.this);
		}
	}

}
