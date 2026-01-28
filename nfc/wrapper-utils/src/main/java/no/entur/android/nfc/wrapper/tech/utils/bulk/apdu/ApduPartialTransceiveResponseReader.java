package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseReader;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

public class ApduPartialTransceiveResponseReader implements PartialTransceiveResponseReader {

    // Response ADPUs: Two status bytes at the end of the response

    private byte[] nextPartCommand;

    private byte[] response = new byte[1024];
    private int offset = 0;

    private final PartialTransceiveResponsePredicate predicate;

    public ApduPartialTransceiveResponseReader(byte[] nextPartCommand, PartialTransceiveResponsePredicate predicate) {
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

        // overwrite last status if present
        int writeOffset = Math.max(0, this.offset - 2);

        System.arraycopy(part, 0, response, writeOffset, part.length);

        this.offset = writeOffset + part.length;

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
