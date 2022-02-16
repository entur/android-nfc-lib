package no.entur.android.nfc.external.service.tag;

import android.os.Bundle;

import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;

public class TagFactory {

	public Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, INfcTag tagService) {
		return new TagImpl(id, techList, bundles, serviceHandle, tagService);
	}

}
