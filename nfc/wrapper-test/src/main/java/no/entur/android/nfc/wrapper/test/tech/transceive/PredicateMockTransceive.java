package no.entur.android.nfc.wrapper.test.tech.transceive;

import androidx.core.util.Predicate;

import java.io.IOException;

public class PredicateMockTransceive implements MockTransceive {

    private final Predicate<byte[]> command;
    private final byte[] response;
    private final boolean raw;
    private final byte[] errorResponse;

    public PredicateMockTransceive(Predicate<byte[]> command, boolean raw, byte[] response, byte[] errorResponse) {
        this.command = command;
        this.raw = raw;
        this.response = response;
        this.errorResponse = errorResponse;
    }

    @Override
    public byte[] transceive(byte[] data, boolean raw) throws IOException {
        if(raw == this.raw && command.test(data)) {
            return response;
        }
        return errorResponse;
    }

}
