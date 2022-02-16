package no.entur.android.nfc.external.acs.tag;

import no.entur.android.nfc.external.service.tag.TagTechnology;

public abstract class DefaultTechnology implements TagTechnology {

	protected final int tagTechnology;
	protected int slotNumber;

	public DefaultTechnology(int tagTechnology, int slotNumber) {
		this.tagTechnology = tagTechnology;
		this.slotNumber = slotNumber;
	}

	@Override
	public int getTagTechnology() {
		return tagTechnology;
	}

	public int getSlotNumber() {
		return slotNumber;
	}
}
