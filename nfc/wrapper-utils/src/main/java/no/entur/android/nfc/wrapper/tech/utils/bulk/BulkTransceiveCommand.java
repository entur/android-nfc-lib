package no.entur.android.nfc.wrapper.tech.utils.bulk;

public class BulkTransceiveCommand {

    private String partialHandlerId;

    private int id;
    private byte[] command;

    // if present and the predicate fails, discontinue sending commands
    private TranscieveResponsePredicate responsePredicate;

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

    public String getPartialHandlerId() {
        return partialHandlerId;
    }

    public void setPartialHandlerId(String partialHandlerId) {
        this.partialHandlerId = partialHandlerId;
    }

    public TranscieveResponsePredicate getResponsePredicate() {
        return responsePredicate;
    }

    public void setResponsePredicate(TranscieveResponsePredicate responsePredicate) {
        this.responsePredicate = responsePredicate;
    }

}