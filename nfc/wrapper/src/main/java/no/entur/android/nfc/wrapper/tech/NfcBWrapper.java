package no.entur.android.nfc.wrapper.tech;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagWrapper;

public class NfcBWrapper extends NfcB {

	protected android.nfc.tech.NfcB delegate;

	public NfcBWrapper(android.nfc.tech.NfcB delegate) {
		this.delegate = delegate;
	}

	@Override
	public byte[] getApplicationData() {
		return delegate.getApplicationData();
	}

	@Override
	public byte[] getProtocolInfo() {
		return delegate.getProtocolInfo();
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
