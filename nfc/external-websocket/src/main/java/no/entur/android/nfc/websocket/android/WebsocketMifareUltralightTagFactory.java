package no.entur.android.nfc.websocket.android;

import android.os.Bundle;

import java.util.List;

import no.entur.android.nfc.external.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

/**
 * MIFARE Ultralight and NTAG are always ISO 14443-A, so always add NfcA, like Android does. The ATR from the remote
 * reader does not always say so: the ACR1552U gives an NTAG216 the generic ATR 3B 86 80 01 80 4F 03 F0 00 00 3B,
 * and the server identifies it from its version.
 */

public class WebsocketMifareUltralightTagFactory extends MifareUltralightTagFactory {

	@Override
	protected void addTechBundles(int type, byte[] id, byte[] atr, List<Bundle> bundles, List<Integer> tech) {
		super.addTechBundles(type, id, atr, bundles, tech);

		if (!tech.contains(TagTechnology.NFC_A)) {
			Bundle nfcA = new Bundle();
			nfcA.putShort(EXTRA_SAK, EXTRA_SAK_VALUE);
			nfcA.putByteArray(EXTRA_ATQA, EXTRA_ATQA_VALUE);

			// first, like for a standard storage card ATR
			bundles.add(0, nfcA);
			tech.add(0, TagTechnology.NFC_A);
		}
	}
}
