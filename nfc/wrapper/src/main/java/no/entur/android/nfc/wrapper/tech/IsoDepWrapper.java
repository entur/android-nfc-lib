package no.entur.android.nfc.wrapper.tech;

import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.wrapper.EmptyTransceiveResponseException;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagWrapper;

public class IsoDepWrapper extends IsoDep {

	protected android.nfc.tech.IsoDep delegate;

	public IsoDepWrapper(android.nfc.tech.IsoDep delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setTimeout(int timeout) {
		Log.d(getClass().getName(), "Set timeout to " + timeout);
		delegate.setTimeout(timeout);
	}

	@Override
	public int getTimeout() {
		return delegate.getTimeout();
	}

	public byte[] getHistoricalBytes() {
		return delegate.getHistoricalBytes();
	}

	public byte[] getHiLayerResponse() {
		return delegate.getHiLayerResponse();
	}

	public byte[] transceive(byte[] data) throws IOException {
		byte[] transceive = delegate.transceive(data);
		if (transceive.length == 0) {
			throw new EmptyTransceiveResponseException();
		}
		return transceive;
	}

    @Override
    public <T> T transceive(Parcelable data) throws IOException {
        throw new RuntimeException("This operation is not supported for native tags");
    }

    @Override
    public boolean supportsTransceive(Class c) throws IOException {
        return false;
    }

    public int getMaxTransceiveLength() {
		return delegate.getMaxTransceiveLength();
	}

	public boolean isExtendedLengthApduSupported() {
		return this.delegate.isExtendedLengthApduSupported();
	}

	@Override
	public Tag getTag() {
		return new TagWrapper(delegate.getTag());
	}

	@Override
	public void connect() throws IOException {
		delegate.connect();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

	@Override
	public boolean isNative() {
		return true;
	}
}
