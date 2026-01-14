package no.entur.android.nfc.external.hid.reader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nfctools.api.TagType;
import org.nfctools.api.detect.DefaultTagTypeDetector;

import no.entur.android.nfc.external.hid.card.Atr210TagTypeDetector;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class TagTypeDetectorTest {

    @Test
    public void testUltralightOutsideSpec() {
        String atr = "3B8F8001804F0CA000000306030005000000006E";
        byte[] bytes = ByteArrayHexStringConverter.hexStringToByteArray(atr);

        Atr210TagTypeDetector detector = new Atr210TagTypeDetector();
        TagType tagType = detector.parseAtr(null, bytes);

        assertEquals(tagType, TagType.MIFARE_ULTRALIGHT);
    }

    @Test
    public void testUltralight2() {
        String atr = "3B8F8001804F0CA0000003060300030000000068";
        byte[] bytes = ByteArrayHexStringConverter.hexStringToByteArray(atr);

        Atr210TagTypeDetector detector = new Atr210TagTypeDetector();
        TagType tagType = detector.parseAtr(null, bytes);

        assertEquals(tagType, TagType.MIFARE_ULTRALIGHT);
    }

}
