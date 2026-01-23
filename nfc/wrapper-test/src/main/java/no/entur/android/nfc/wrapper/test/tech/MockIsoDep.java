package no.entur.android.nfc.wrapper.test.tech;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockIsoDep extends MockBasicTagTechnologyImpl {

    private byte[] hiLayerResponse;
    private byte[] historicalBytes;

    private MockParcelableTransceive mockParcelableTransceive;

    public MockIsoDep(byte[] hiLayerResponse, byte[] historicalBytes, MockParcelableTransceive mockParcelableTransceive) {
        super(BasicTagTechnology.ISO_DEP, mockParcelableTransceive);

        this.hiLayerResponse = hiLayerResponse;
        this.historicalBytes = historicalBytes;

        this.mockParcelableTransceive = mockParcelableTransceive;
    }

    public byte[] getHiLayerResponse() {
        return hiLayerResponse;
    }

    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }

    public MockParcelableTransceive getMockParcelableTransceive() {
        return mockParcelableTransceive;
    }

    public void setMockParcelableTransceive(MockParcelableTransceive mockParcelableTransceive) {
        this.mockParcelableTransceive = mockParcelableTransceive;
    }


    public <T> T transceive(Parcelable parcelable) throws IOException {
        return (T)this.mockParcelableTransceive.parcelableTranscieve(parcelable);
    }

    public <T> T  parcelableTransceiveMetadata(Parcelable parcelable) throws IOException {
        return (T)this.mockParcelableTransceive.parcelableTranscieve(parcelable);
    }
}
