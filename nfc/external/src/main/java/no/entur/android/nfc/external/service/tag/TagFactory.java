package no.entur.android.nfc.external.service.tag;

import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public abstract class TagFactory {

	public Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, INfcTag tagService) {
		return new TagImpl(id, techList, bundles, serviceHandle, tagService);
	}

	protected Intent getIntent(List<Bundle> bundles, List<Integer> tech) {
		if(tech.contains(TagTechnology.NDEF)) {
			return new Intent(ExternalNfcTagCallback.ACTION_NDEF_DISCOVERED);
		} else {
			return new Intent(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED);
		}
	}

}
