package no.entur.android.nfc.detect.app;

public class SelectApplicationAnalyzeResult {

    private final byte[] applicationId;
    private final boolean success;

    // preserve commands
    private final byte[] command;
    private final byte[] response;

    public SelectApplicationAnalyzeResult(boolean success, byte[] applicationId, byte[] command, byte[] response) {
        this.applicationId = applicationId;
        this.success = success;
        this.command = command;
        this.response = response;
    }

    public byte[] getApplicationId() {
        return applicationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getCommand() {
        return command;
    }

    public byte[] getResponse() {
        return response;
    }
}
