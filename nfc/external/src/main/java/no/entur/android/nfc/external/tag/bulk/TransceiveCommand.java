package no.entur.android.nfc.external.tag.bulk;


import no.entur.android.nfc.external.tag.bulk.chunked.ChunkedTransceiveResponseHandler;
import no.entur.android.nfc.external.tag.bulk.validate.TransceiveResponseValidator;

public class TransceiveCommand {

    private int id;
    private byte[] frame;

    private ChunkedTransceiveResponseHandler chunkedTransceiveResponseHandler;

    private TransceiveResponseValidator transceiveResponseValidator;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame) {
        this.frame = frame;
    }

    public ChunkedTransceiveResponseHandler getChunkedTransceiveResponseHandler() {
        return chunkedTransceiveResponseHandler;
    }

    public void setChunkedTransceiveResponseHandler(ChunkedTransceiveResponseHandler chunkedTransceiveResponseHandler) {
        this.chunkedTransceiveResponseHandler = chunkedTransceiveResponseHandler;
    }

    public TransceiveResponseValidator getTransceiveResponseValidator() {
        return transceiveResponseValidator;
    }

    public void setTransceiveResponseValidator(TransceiveResponseValidator transceiveResponseValidator) {
        this.transceiveResponseValidator = transceiveResponseValidator;
    }
}
