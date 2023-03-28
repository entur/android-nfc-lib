package no.entur.android.nfc.external.minova;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.nfctools.api.TagType;

import no.entur.android.nfc.external.minova.service.MinovaTagType;
import no.entur.android.nfc.external.minova.service.MinovaTagTypeDetector;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaTagTypeDetectorTest {

    private MinovaTagTypeDetector tagTypeDetector = new MinovaTagTypeDetector();

    @Test
    public void parseEmv() {
        // danske bank
        String response = "0004;20;10788070020031c16408603206009000";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
        assertNotNull(tagType.getHistoricalBytes());
    }

    @Test
    public void parseTravelcard() {
        String response = "0344;20;067577810280";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
        assertNotNull(tagType.getHistoricalBytes()); // i.e. 80
    }

    @Test
    public void parseArsenalMemberCard() {
        String response = "0004;08;00";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertNotEquals(tagType.getTagType(), TagType.ISO_DEP);
    }

    @Test
    public void parseOldEmv() {
        String response = "0044;20;00";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
    }

    @Test
    public void parseUnknownCardDefaultToIsoDep() {
        // danske bank
        String response = "0004;FE;10788070020031c16408603206009000";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
        assertNotNull(tagType.getHistoricalBytes());
    }

    // TODO HCE target

}
