package no.entur.android.nfc.detect.app;

public class SelectApplicationAnalyzeResult {

    private final byte[] applicationId;
    private final byte[] response; // response payload, i.e. without status codes
    private final boolean success;

    public SelectApplicationAnalyzeResult(boolean success, byte[] applicationId, byte[] response) {
        this.applicationId = applicationId;
        this.response = response;
        this.success = success;
    }

    public byte[] getApplicationId() {
        return applicationId;
    }

    public byte[] getResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }
}
