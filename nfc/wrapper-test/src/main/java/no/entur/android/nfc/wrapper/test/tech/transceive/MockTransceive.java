package no.entur.android.nfc.wrapper.test.tech.transceive;

import java.io.IOException;

public interface MockTransceive {

    byte[] transceive(byte[] data, boolean raw) throws IOException;

}
