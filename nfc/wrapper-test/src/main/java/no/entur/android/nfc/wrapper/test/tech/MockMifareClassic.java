package no.entur.android.nfc.wrapper.test.tech;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockMifareClassic extends MockBasicTagTechnologyImpl {


    public MockMifareClassic(MockTransceive transceive) {
        super(BasicTagTechnology.MIFARE_CLASSIC, transceive);
    }

}
