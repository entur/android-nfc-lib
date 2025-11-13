package no.entur.android.nfc.wrapper.test.tech;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNfcB extends MockBasicTagTechnologyImpl {

    public MockNfcB(MockTransceive transceive) {
        super(BasicTagTechnology.NFC_B, transceive);
    }
}
