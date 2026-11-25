package no.entur.android.nfc.hce;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.detect.technology.HostCardEmulationTechnologyAnalyzer;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class IsoDepDeviceHintTest {

    private static IsoDep isoDep(byte[] id, byte[] historicalBytes) {
        Tag tag = mock(Tag.class);
        when(tag.getId()).thenReturn(id);

        IsoDep isoDep = mock(IsoDep.class);
        when(isoDep.getTag()).thenReturn(tag);
        when(isoDep.getHistoricalBytes()).thenReturn(historicalBytes);
        return isoDep;
    }

    @Test
    public void testNfcB() {
        // historical bytes are null for NFC-B
        IsoDepDeviceHint hint = new IsoDepDeviceHint(isoDep(new byte[] { 0x08, 1, 2, 3 }, null));

        assertFalse(hint.isTag());
        assertFalse(hint.isHostCardEmulation());
        assertNull(hint.guessType());
    }

    @Test
    public void testHostCardEmulation() {
        IsoDepDeviceHint hint = new IsoDepDeviceHint(isoDep(new byte[] { 0x08, 1, 2, 3 }, new byte[0]));

        assertEquals(IsoDepDeviceType.HOST_CARD_EMULATION, hint.guessType());
    }

    @Test
    public void testDesfire() {
        IsoDepDeviceHint hint = new IsoDepDeviceHint(isoDep(new byte[] { 0x04, 1, 2, 3, 4, 5, 6 }, new byte[] { (byte) 0x80 }));

        assertTrue(hint.isTag());
    }

    @Test
    public void testHostCardEmulationTechnologyAnalyzer() {
        HostCardEmulationTechnologyAnalyzer analyzer = new HostCardEmulationTechnologyAnalyzer();

        TagTechnologies nfcB = new TagTechnologies();
        nfcB.setIsoDep(isoDep(new byte[] { 0x08, 1, 2, 3 }, null));
        assertFalse(analyzer.processTechnology(nfcB, null, null).hasType());

        TagTechnologies hce = new TagTechnologies();
        hce.setIsoDep(isoDep(new byte[] { 0x08, 1, 2, 3 }, new byte[0]));
        assertTrue(analyzer.processTechnology(hce, null, null).hasType());
    }
}
