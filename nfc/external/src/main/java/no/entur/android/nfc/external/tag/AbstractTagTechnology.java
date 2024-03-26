package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.external.service.tag.TagTechnology;

public abstract class AbstractTagTechnology implements TagTechnology {

	protected final int tagTechnology;

	public AbstractTagTechnology(int tagTechnology) {
		this.tagTechnology = tagTechnology;
	}

	@Override
	public int getTagTechnology() {
		return tagTechnology;
	}


}
