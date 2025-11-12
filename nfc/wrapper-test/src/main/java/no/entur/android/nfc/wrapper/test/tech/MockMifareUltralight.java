package no.entur.android.nfc.wrapper.test.tech;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockMifareUltralight extends MockBasicTagTechnologyImpl {

    private int type;

    public MockMifareUltralight(int type, MockTransceive mockTransceive) {
        super(BasicTagTechnology.MIFARE_ULTRALIGHT, mockTransceive);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public MockTransceive getTransceive() {
        return transceive;
    }
}
