package no.entur.android.nfc.external.tag;

public abstract class AbstractReaderIsoDepWrapper {

	private static final String TAG = AbstractReaderIsoDepWrapper.class.getName();

	protected int slotNum;

	public AbstractReaderIsoDepWrapper(int slotNum) {
		this.slotNum = slotNum;
	}

	// this might also work for raw data
	public abstract byte[] transceive(byte[] data) throws Exception;

	public abstract byte[] transceiveRaw(byte[] data) throws Exception;
}
