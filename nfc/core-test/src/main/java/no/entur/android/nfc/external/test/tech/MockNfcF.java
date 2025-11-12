package no.entur.android.nfc.external.test.tech;

import java.util.List;

import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNfcF extends MockBasicTagTechnologyImpl {
    public MockNfcF(MockTransceive transceive) {
        super(BasicTagTechnology.NDEF, transceive);
    }
}
