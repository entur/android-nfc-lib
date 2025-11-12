package no.entur.android.nfc.external.test.tech;

import java.util.List;

import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNfcA extends MockBasicTagTechnologyImpl {
    public MockNfcA(MockTransceive transceive) {
        super(BasicTagTechnology.NFC_A, transceive);
    }
}
