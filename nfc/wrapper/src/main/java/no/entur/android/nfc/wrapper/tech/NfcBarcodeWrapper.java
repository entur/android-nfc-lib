package no.entur.android.nfc.wrapper.tech;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagWrapper;

import java.io.IOException;

public class NfcBarcodeWrapper extends NfcBarcode {

    protected android.nfc.tech.NfcBarcode delegate;

    public NfcBarcodeWrapper(android.nfc.tech.NfcBarcode delegate) {
        this.delegate = delegate;
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
    public int getType() {
        return delegate.getType();
    }

    @Override
    public byte[] getBarcode() {
        return delegate.getBarcode();
    }
}
