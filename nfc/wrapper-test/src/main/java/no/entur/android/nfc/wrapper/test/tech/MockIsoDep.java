package no.entur.android.nfc.wrapper.test.tech;

import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockIsoDep extends MockBasicTagTechnologyImpl {

    private static final String LOG_TAG = MockIsoDep.class.getName();

    private byte[] hiLayerResponse;
    private byte[] historicalBytes;

    private MockParcelableTransceive mockParcelableTransceive;

    public MockIsoDep(byte[] hiLayerResponse, byte[] historicalBytes, MockTransceive mockTransceive, MockParcelableTransceive mockParcelableTransceive) {
        super(BasicTagTechnology.ISO_DEP, mockTransceive);

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

    public boolean supportsTransceiveParcelable(String cls) throws IOException {
        return this.mockParcelableTransceive.supportsTransceiveParcelable(cls);
    }
}
