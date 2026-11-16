package no.entur.android.nfc.detect.uid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.entur.android.nfc.wrapper.Tag;

public class UidAnalyzerTest {

    private static Tag tag(byte[] id) {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(id);
        return tag;
    }

    @Test
    public void testEmptyUid() {
        UidAnalyzeResult nxp = new SevenByteNxpUidAnalyzer().processUid(null, tag(new byte[0]), null);
        assertFalse(nxp.isLength());
        assertEquals(UidManufacturerType.NOT_AVAILABLE, nxp.getManufacturerType());

        UidAnalyzeResult hce = new HostCardEmulationUidAnalyzer().processUid(null, tag(new byte[0]), null);
        assertFalse(hce.isLength());
        assertEquals(UidManufacturerType.NOT_AVAILABLE, hce.getManufacturerType());
    }

    @Test
    public void testUid() {
        UidAnalyzeResult nxp = new SevenByteNxpUidAnalyzer().processUid(null, tag(new byte[] { 0x04, 1, 2, 3, 4, 5, 6 }), null);
        assertTrue(nxp.isLength());
        assertEquals(UidManufacturerType.MATCH, nxp.getManufacturerType());

        UidAnalyzeResult hce = new HostCardEmulationUidAnalyzer().processUid(null, tag(new byte[] { 0x08, 1, 2, 3 }), null);
        assertTrue(hce.isLength());
        assertEquals(UidManufacturerType.MATCH, hce.getManufacturerType());
    }
}
