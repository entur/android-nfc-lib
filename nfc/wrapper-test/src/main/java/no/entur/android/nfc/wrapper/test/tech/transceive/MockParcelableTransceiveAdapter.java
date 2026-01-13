package no.entur.android.nfc.wrapper.test.tech.transceive;

import android.os.Parcelable;

import java.io.IOException;

public class MockParcelableTransceiveAdapter implements MockParcelableTransceive {

    private final MockTransceive transceive;

    public MockParcelableTransceiveAdapter(MockTransceive transceive) {
        this.transceive = transceive;
    }

    @Override
    public <T> T parcelableTranscieve(Parcelable data) throws IOException {
        throw new RuntimeException("Unexpected call");
    }

    @Override
    public boolean supportsTransceiveParcelable(String className) throws IOException {
        return false;
    }

    @Override
    public byte[] transceive(byte[] data, boolean raw) throws IOException {
        return transceive.transceive(data, raw);
    }
}
