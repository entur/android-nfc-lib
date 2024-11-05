package no.entur.android.nfc.detect.app;

public class SelectApplicationAnalyzeResult {

    private final byte[] applicationId;
    private final byte[] selectApplicationResponseAdpu;

    public SelectApplicationAnalyzeResult(byte[] applicationId, byte[] selectApplicationResponseAdpu) {
        this.applicationId = applicationId;
        this.selectApplicationResponseAdpu = selectApplicationResponseAdpu;
    }

    public byte[] getApplicationId() {
        return applicationId;
    }

    public byte[] getSelectApplicationResponseAdpu() {
        return selectApplicationResponseAdpu;
    }
}
