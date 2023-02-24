package no.entur.android.nfc.external.minova;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.nfctools.api.TagType;

import no.entur.android.nfc.external.minova.service.MinovaTagType;
import no.entur.android.nfc.external.minova.service.MinovaTagTypeDetector;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaTagTypeDetectorTest {

    private MinovaTagTypeDetector tagTypeDetector = new MinovaTagTypeDetector();

    @Test
    public void parseTestIso4() {
        String response = "0344;20;067577810280";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
    }

    @Test
    public void parseTestIso3() {
        String response = "0004;08;00";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertNotEquals(tagType.getTagType(), TagType.ISO_DEP);
    }

}
