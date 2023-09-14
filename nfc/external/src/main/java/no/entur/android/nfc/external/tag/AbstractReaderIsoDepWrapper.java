package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.external.service.tag.TagProxy;

public abstract class AbstractReaderIsoDepWrapper {

	private static final String TAG = AbstractReaderIsoDepWrapper.class.getName();

	protected int slotNum;
	protected TagProxy tagProxy;

	public AbstractReaderIsoDepWrapper(int slotNum) {
		this.slotNum = slotNum;
	}

	public void setTagProxy(TagProxy tagProxy) {
		this.tagProxy = tagProxy;
	}

	// this might also work for raw data
	public abstract byte[] transceive(byte[] data) throws Exception;

	public abstract byte[] transceiveRaw(byte[] data) throws Exception;
}
