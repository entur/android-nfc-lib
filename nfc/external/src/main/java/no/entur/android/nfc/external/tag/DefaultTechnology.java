package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.external.service.tag.TagTechnology;

public abstract class DefaultTechnology implements TagTechnology {

	protected final int tagTechnology;

	public DefaultTechnology(int tagTechnology) {
		this.tagTechnology = tagTechnology;
	}

	@Override
	public int getTagTechnology() {
		return tagTechnology;
	}


}
