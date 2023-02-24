package no.entur.android.nfc.external.tag;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.service.tag.TagFactory;
import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

/**
 * http://nfc-tools.org/index.php?title=ISO14443A
 *
 */

public class MifareDesfireTagFactory extends TagFactory {

	private static final String TAG = MifareDesfireTagFactory.class.getName();

	/**
	 * @hide
	 */
	public static final String EXTRA_HI_LAYER_RESP = "hiresp";
	/**
	 * @hide
	 */
	public static final String EXTRA_HIST_BYTES = "histbytes";

	protected static final String EXTRA_SAK = "sak";
	protected static final String EXTRA_ATQA = "atqa";

	protected static final byte[] EXTRA_ATQA_VALUE = new byte[] { 0x44, 0x03 };
	protected static final short EXTRA_SAK_VALUE = 0x20;


	public Intent getTag(int serviceHandle, byte[] hiLayer, byte[] id, boolean hce, byte[] historicalBytes, INfcTag tagService, IntentEnricher extras) {

		/*
		 * if (id != null && id[0] != NXP_MANUFACTURER_ID) { throw new IllegalArgumentException("Non-NXP tag id"); }
		 */

		List<Bundle> bundles = new ArrayList<Bundle>();
		List<Integer> tech = new ArrayList<Integer>();

		addTechBundles(hiLayer, historicalBytes, bundles, tech);

		final Intent intent = getIntent(bundles, tech);

		int[] techArray = new int[tech.size()];
		for (int i = 0; i < techArray.length; i++) {
			techArray[i] = tech.get(i);
		}

		intent.putExtra(NfcAdapter.EXTRA_TAG, createTag(id, techArray, bundles.toArray(new Bundle[bundles.size()]), serviceHandle, tagService));
		if (id != null) {
			intent.putExtra(NfcAdapter.EXTRA_ID, id);
		}

		return extras.enrich(intent);

	}

	private void addTechBundles(byte[] hiLayer, byte[] historicalBytes, List<Bundle> bundles, List<Integer> tech) {
		Bundle nfcA = new Bundle();
		nfcA.putShort(EXTRA_SAK, EXTRA_SAK_VALUE);
		nfcA.putByteArray(EXTRA_ATQA, EXTRA_ATQA_VALUE);
		bundles.add(nfcA);
		tech.add(TagTechnology.NFC_A);

		Bundle desfire = new Bundle();
		desfire.putByteArray(EXTRA_HIST_BYTES, historicalBytes);
		if (hiLayer != null) {
			desfire.putByteArray(EXTRA_HI_LAYER_RESP, hiLayer);
		}
		bundles.add(desfire);
		tech.add(TagTechnology.ISO_DEP);
	}

}
