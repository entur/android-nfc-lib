package no.entur.android.nfc.external.test.tech.transceive;

import androidx.core.util.Predicate;

import java.io.IOException;

import no.entur.android.nfc.external.test.tech.MockBasicTagTechnologyImpl;

public class PredicateMockTransceive implements MockTransceive {

    private final Predicate<byte[]> command;
    private final byte[] response;

    private final byte[] errorResponse;

    public PredicateMockTransceive(Predicate<byte[]> command, byte[] response, byte[] errorResponse) {
        this.command = command;
        this.response = response;
        this.errorResponse = errorResponse;
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        if(command.test(data)) {
            return response;
        }
        return errorResponse;
    }

}
