package no.entur.android.nfc.wrapper.tech.utils.bulk;

public class BulkTransceiveCommand {

    private int id;
    private byte[] command;

    // if present and the predicate fails, discontinue sending commands
    private TranscieveResponsePredicate responsePredicate;

    private PartialTranscieveResponsePredicate partialTranscieveResponsePredicate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        this.command = command;
    }

    public TranscieveResponsePredicate getResponsePredicate() {
        return responsePredicate;
    }

    public void setResponsePredicate(TranscieveResponsePredicate responsePredicate) {
        this.responsePredicate = responsePredicate;
    }

    public PartialTranscieveResponsePredicate getPartialTranscieveResponsePredicate() {
        return partialTranscieveResponsePredicate;
    }

    public void setPartialTranscieveResponsePredicate(PartialTranscieveResponsePredicate partialTranscieveResponsePredicate) {
        this.partialTranscieveResponsePredicate = partialTranscieveResponsePredicate;
    }
}