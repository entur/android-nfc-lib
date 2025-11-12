package no.entur.android.nfc.external.test.tech;

import java.util.List;

import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockIsoDep extends MockBasicTagTechnologyImpl {

    private byte[] hiLayerResponse;
    private byte[] historicalBytes;

    public MockIsoDep(byte[] hiLayerResponse, byte[] historicalBytes, MockTransceive mockTransceive) {
        super(BasicTagTechnology.ISO_DEP, mockTransceive);

        this.hiLayerResponse = hiLayerResponse;
        this.historicalBytes = historicalBytes;
    }

    public byte[] getHiLayerResponse() {
        return hiLayerResponse;
    }

    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }
}
