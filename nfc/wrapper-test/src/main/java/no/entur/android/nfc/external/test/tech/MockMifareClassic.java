package no.entur.android.nfc.external.test.tech;

import java.util.List;

import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockMifareClassic extends MockBasicTagTechnologyImpl {


    public MockMifareClassic(MockTransceive transceive) {
        super(BasicTagTechnology.MIFARE_CLASSIC, transceive);
    }

}
