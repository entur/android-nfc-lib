package no.entur.android.nfc.wrapper.tech;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagWrapper;

public class NfcAWrapper extends NfcA {

	protected android.nfc.tech.NfcA delegate;

	public NfcAWrapper(android.nfc.tech.NfcA delegate) {
		this.delegate = delegate;
	}

	@Override
	public byte[] getAtqa() {
		return delegate.getAtqa();
	}

	@Override
	public short getSak() {
		return delegate.getSak();
	}

	@Override
	public byte[] transceive(byte[] data) throws IOException {
		return delegate.transceive(data);
	}

	@Override
	public int getMaxTransceiveLength() {
		return delegate.getMaxTransceiveLength();
	}

	@Override
	public void setTimeout(int timeout) {
		delegate.setTimeout(timeout);
	}

	@Override
	public int getTimeout() {
		return delegate.getTimeout();
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
}
