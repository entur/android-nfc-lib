package no.entur.android.nfc.detect.app;

public class SelectApplicationAnalyzeResult {

    private final byte[] applicationId;
    private final boolean success;

    // preserve commands
    private final byte[] commandAdpu;
    private final byte[] responseAdpu;

    public SelectApplicationAnalyzeResult(boolean success, byte[] applicationId, byte[] commandAdpu, byte[] responseAdpu) {
        this.applicationId = applicationId;
        this.success = success;
        this.commandAdpu = commandAdpu;
        this.responseAdpu = responseAdpu;
    }

    public byte[] getApplicationId() {
        return applicationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getCommandAdpu() {
        return commandAdpu;
    }

    public byte[] getResponseAdpu() {
        return responseAdpu;
    }
}
