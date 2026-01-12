package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseHandler;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class NativeMifareDesfireEV1PartialTranscieveResponseHandler implements PartialTranscieveResponseHandler {

    // Response ADPUs: One status byte at the start of the response

    private byte[] nextPartCommand;

    private byte[] response = new byte[1024];
    private int offset = 1;

    private final PartialTranscieveResponsePredicate predicate;

    public NativeMifareDesfireEV1PartialTranscieveResponseHandler(byte[] nextPartCommand, PartialTranscieveResponsePredicate predicate) {
        super();

        this.nextPartCommand = nextPartCommand;
        this.predicate = predicate;
    }

    @Override
    public byte[] next(byte[] part) {

        if(part.length + offset > response.length) {
            byte[] nextResponse = new byte[response.length * 2];

            System.arraycopy(response, 0, nextResponse, 0, offset);

            this.response = nextResponse;
        }

        // copy body
        System.arraycopy(part, 1, response, offset, part.length - 1);
        // copy last status
        response[0] = part[0];

        this.offset = offset + part.length  -1;

        if(predicate.test(part)) {
            return nextPartCommand;
        }

        return null;
    }

    @Override
    public byte[] assemble() {
        byte[] parts = new byte[offset];
        System.arraycopy(response, 0, parts, 0, parts.length);
        return parts;
    }

}
