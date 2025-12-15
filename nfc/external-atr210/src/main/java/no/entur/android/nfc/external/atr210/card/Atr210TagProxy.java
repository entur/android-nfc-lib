package no.entur.android.nfc.external.atr210.card;

import java.util.List;

import no.entur.android.nfc.external.service.tag.NdefTechnology;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.wrapper.TagImpl;

public class Atr210TagProxy implements TagProxy {

	private int handle;
	private List<TagTechnology> technologies;

	private TagTechnology current;

	private TagProxyStore tagProxyStore;

	private boolean present = true;

	public Atr210TagProxy(int handle, List<TagTechnology> technologies, TagProxyStore tagProxyStore) {
		this.handle = handle;
		this.technologies = technologies;
		this.tagProxyStore = tagProxyStore;
	}

	public TagTechnology getCurrent() {
		return current;
	}

	public void setCurrent(TagTechnology current) {
		this.current = current;
	}

	public int getHandle() {
		return handle;
	}

	public void setHandle(int id) {
		this.handle = id;
	}

	public List<TagTechnology> getTechnologies() {
		return technologies;
	}

	public void setTechnologies(List<TagTechnology> technologies) {
		this.technologies = technologies;
	}

	public boolean add(TagTechnology object) {
		return technologies.add(object);
	}

	public TagTechnology getTechnology(int technology) {
		for (TagTechnology tech : technologies) {
			if (tech.getTagTechnology() == technology) {
				return tech;
			}
		}
		return null;
	}

	public boolean selectTechnology(int technology) {
		for (TagTechnology tech : technologies) {
			if (tech.getTagTechnology() == technology) {
				this.current = tech;

				return true;
			}
		}

		return false;
	}

	public void closeTechnology() {
		this.current = null;
	}

	public TagImpl rediscover(Object callback) {
		throw new RuntimeException();
	}

	public boolean isPresent() {
		return present;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}

	@Override
	public int getSlotNumber() {
		return 0;
	}

	public boolean isNdef() {
		for(TagTechnology t : technologies) {
			if(t instanceof NdefTechnology) {
				return true;
			}
		}
		return false;
	}

	public boolean ndefIsWritable() {
		for(TagTechnology t : technologies) {
			if(t instanceof NdefTechnology) {
				NdefTechnology ndef = (NdefTechnology)t;
				return ndef.isWritable();
			}
		}
		return false;
	}

	public void close() {
		tagProxyStore.remove(this);
	}
}
