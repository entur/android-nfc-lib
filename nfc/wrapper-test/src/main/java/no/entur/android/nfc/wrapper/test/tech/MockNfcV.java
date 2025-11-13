package no.entur.android.nfc.wrapper.test.tech;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNfcV extends MockBasicTagTechnologyImpl {
    public MockNfcV(MockTransceive transceive) {
        super(BasicTagTechnology.NFC_V, transceive);
    }
}
