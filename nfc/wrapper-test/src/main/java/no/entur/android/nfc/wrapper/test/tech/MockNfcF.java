package no.entur.android.nfc.wrapper.test.tech;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNfcF extends MockBasicTagTechnologyImpl {
    public MockNfcF(MockTransceive transceive) {
        super(BasicTagTechnology.NDEF, transceive);
    }
}
