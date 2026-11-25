package no.entur.android.nfc.external.minova;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    @Test
    public void parseIsoDepWithoutInterfaceBytes() {
        // T0 = 0x00: no TA(1), TB(1) or TC(1)
        String response = "0344;20;030080";

        MinovaTagType tagType = tagTypeDetector.getTagType(response);
        assertEquals(tagType.getTagType(), TagType.ISO_DEP);
        assertArrayEquals(new byte[]{(byte) 0x80}, tagType.getHistoricalBytes());
    }

    @Test
    public void historicalBytesWithAllInterfaceBytes() {
        // T0 = 0x75: TA(1), TB(1) and TC(1)
        assertArrayEquals(new byte[]{(byte) 0x80}, MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("067577810280")));
    }

    @Test
    public void historicalBytesWithSomeInterfaceBytes() {
        // T0 = 0x58: TA(1) and TC(1), but not TB(1)
        assertArrayEquals(ByteArrayHexStringConverter.hexStringToByteArray("8031C1"), MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("0758800280" + "31C1")));
        // T0 = 0x20: TB(1) only
        assertArrayEquals(ByteArrayHexStringConverter.hexStringToByteArray("8031"), MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("0520818031")));
    }

    @Test
    public void historicalBytesWithoutHistoricalBytes() {
        assertArrayEquals(new byte[0], MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("0578807002")));
        assertArrayEquals(new byte[0], MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("0200")));
        assertArrayEquals(new byte[0], MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("01")));
    }

    @Test
    public void historicalBytesForMalformedAts() {
        // length byte does not match
        assertNull(MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("0775778102")));
        // interface bytes missing
        assertNull(MinovaTagTypeDetector.getHistoricalBytes(ByteArrayHexStringConverter.hexStringToByteArray("037077")));
        assertNull(MinovaTagTypeDetector.getHistoricalBytes(new byte[0]));
        assertNull(MinovaTagTypeDetector.getHistoricalBytes(null));
    }

    // TODO HCE target

}
