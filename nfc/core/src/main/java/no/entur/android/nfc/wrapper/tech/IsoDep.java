package no.entur.android.nfc.wrapper.tech;

import android.os.RemoteException;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TagWrapper;

public abstract class IsoDep implements BasicTagTechnology {

	/**
	 * Get an instance of {@link IsoDepImpl} for the given tag.
	 * <p>
	 * Does not cause any RF activity and does not block.
	 * <p>
	 * Returns null if {@link IsoDepImpl} was not enumerated in {@link TagImpl#getTechList}. This indicates the tag does not support ISO-DEP.
	 *
	 * @param tag an ISO-DEP compatible tag
	 * @return ISO-DEP object
	 */
	public static IsoDep get(Tag tag) {
		if (tag instanceof TagImpl) {
			TagImpl tagImpl = (TagImpl) tag;
			if (!tagImpl.hasTech(TagTechnology.ISO_DEP))
				return null;
			try {
				return new IsoDepImpl(tagImpl);
			} catch (RemoteException e) {
				return null;
			}
		} else if (tag instanceof TagWrapper) {
			TagWrapper tagWrapper = (TagWrapper) tag;

			android.nfc.Tag delegate = tagWrapper.getDelegate();
			android.nfc.tech.IsoDep isoDep = android.nfc.tech.IsoDep.get(delegate);
			if (isoDep == null) {
				return null;
			}
			return new IsoDepWrapper(isoDep);
		} else {
			throw new IllegalArgumentException("Unexpected instance " + tag);
		}
	}

	public abstract void setTimeout(int timeout);

	public abstract int getTimeout();

	public abstract byte[] getHistoricalBytes();

	public abstract byte[] getHiLayerResponse();

	public abstract byte[] transceive(byte[] data) throws IOException;

	public abstract int getMaxTransceiveLength();

	public abstract boolean isExtendedLengthApduSupported();
}
