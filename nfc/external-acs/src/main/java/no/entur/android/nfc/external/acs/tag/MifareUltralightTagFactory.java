package no.entur.android.nfc.external.acs.tag;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.service.tag.TagFactory;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

/**
 * http://nfc-tools.org/index.php?title=ISO14443A
 *
 */

public class MifareUltralightTagFactory extends TagFactory {

	public static final int NXP_MANUFACTURER_ID = 0x04;

	protected static final String EXTRA_SAK = "sak";
	protected static final String EXTRA_ATQA = "atqa";

	protected static final byte[] EXTRA_ATQA_VALUE = new byte[] { 0x44, 0x00 };
	protected static final short EXTRA_SAK_VALUE = 0;

	/**
	 * A MIFARE Ultralight compatible tag of unknown type
	 */
	public static final int TYPE_UNKNOWN = -1;
	/**
	 * A MIFARE Ultralight tag
	 */
	public static final int TYPE_ULTRALIGHT = 1;
	/**
	 * A MIFARE Ultralight C tag
	 */
	public static final int TYPE_ULTRALIGHT_C = 2;

	public static final String EXTRA_IS_UL_C = "isulc";

	/** NDEF */
	/**
	 * @hide
	 */
	public static final int NDEF_MODE_READ_ONLY = 1;
	/**
	 * @hide
	 */
	public static final int NDEF_MODE_READ_WRITE = 2;
	/**
	 * @hide
	 */
	public static final int NDEF_MODE_UNKNOWN = 3;

	public static final String EXTRA_NDEF_MSG = "ndefmsg";

	/**
	 * @hide
	 */
	public static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";

	/**
	 * @hide
	 */
	public static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";

	/**
	 * @hide
	 */
	public static final String EXTRA_NDEF_TYPE = "ndeftype";

	/**
	 * @hide
	 */
	public static final int TYPE_OTHER = -1;
	/**
	 * @hide
	 */
	public static final int TYPE_1 = 1;
	/**
	 * @hide
	 */
	public static final int TYPE_2 = 2;
	/**
	 * @hide
	 */
	public static final int TYPE_3 = 3;
	/**
	 * @hide
	 */
	public static final int TYPE_4 = 4;
	/**
	 * @hide
	 */
	public static final int TYPE_MIFARE_CLASSIC = 101;
	/**
	 * @hide
	 */
	public static final int TYPE_ICODE_SLI = 102;

	/*
	 * private int serviceHandle; private int type; private byte[] id; private int maxNdefSize; private NdefMessage message; private Boolean writable;
	 * 
	 * public MifareUltralightTagFactory(int serviceHandle, int type, byte[] id, int maxNdefSize, NdefMessage message, Boolean writable) { this.serviceHandle =
	 * serviceHandle; this.type = type; this.id = id; this.maxNdefSize = maxNdefSize; this.message = message; this.writable = writable; }
	 */

	public Intent getTag(int serviceHandle, int slotNumber, int type, Integer ntagType, byte[] id, byte[] atr, INfcTag tagService) {

		if (id != null) {
			if (id[0] != NXP_MANUFACTURER_ID) {
				throw new IllegalArgumentException("Non-NXP tag id " + ByteArrayHexStringConverter.toHexString(id));
			}
		}

		List<Bundle> bundles = new ArrayList<Bundle>();
		List<Integer> tech = new ArrayList<Integer>();

		if (TechnologyType.isNFCA(atr)) {
			Bundle nfcA = new Bundle();
			nfcA.putShort(EXTRA_SAK, EXTRA_SAK_VALUE);
			nfcA.putByteArray(EXTRA_ATQA, EXTRA_ATQA_VALUE);
			bundles.add(nfcA);
			tech.add(TagTechnology.NFC_A);
		}

		if (id != null) {
			Bundle ultralight = new Bundle();
			ultralight.putBoolean(EXTRA_IS_UL_C, type == TYPE_ULTRALIGHT_C);
			bundles.add(ultralight);
			tech.add(TagTechnology.MIFARE_ULTRALIGHT);
		}

		final Intent intent = new Intent(ExternalNfcReaderCallback.ACTION_TAG_DISCOVERED);

		Bundle ndefFormatable = new Bundle();
		bundles.add(ndefFormatable);

		tech.add(TagTechnology.NDEF_FORMATABLE);

		int[] techArray = new int[tech.size()];
		for (int i = 0; i < techArray.length; i++) {
			techArray[i] = tech.get(i);
		}

		intent.putExtra(NfcAdapter.EXTRA_TAG, createTag(id, techArray, bundles.toArray(new Bundle[bundles.size()]), serviceHandle, tagService));
		intent.putExtra(NfcAdapter.EXTRA_ID, id);

		return intent;

	}

}
