package no.entur.android.nfc.websocket.android;

import static org.junit.Assert.assertEquals;

import android.os.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.entur.android.nfc.external.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class WebsocketMifareUltralightTagFactoryTest {

    private static final byte[] UID = new byte[]{0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66};

    // NTAG216 on an ACR1552U; the server identifies it as MIFARE Ultralight from its version
    static final byte[] GENERIC_NTAG_ATR = new byte[]{0x3B, (byte) 0x86, (byte) 0x80, 0x01, (byte) 0x80, 0x4F, 0x03, (byte) 0xF0, 0x00, 0x00, 0x3B};

    private static final byte[] ULTRALIGHT_ATR = new byte[]{0x3B, (byte) 0x8F, (byte) 0x80, 0x01, (byte) 0x80, 0x4F, 0x0C, (byte) 0xA0, 0x00, 0x00, 0x03, 0x06, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x68};

    @Test
    public void testNfcAForGenericAtr() {
        assertEquals(Arrays.asList(TagTechnology.NFC_A, TagTechnology.MIFARE_ULTRALIGHT), getTechnologies(GENERIC_NTAG_ATR));
    }

    @Test
    public void testNfcAOnceForStorageCardAtr() {
        assertEquals(Arrays.asList(TagTechnology.NFC_A, TagTechnology.MIFARE_ULTRALIGHT), getTechnologies(ULTRALIGHT_ATR));
    }

    private static List<Integer> getTechnologies(byte[] atr) {
        List<Bundle> bundles = new ArrayList<>();
        List<Integer> tech = new ArrayList<>();

        new WebsocketMifareUltralightTagFactory().addTechBundles(MifareUltralightTagFactory.TYPE_ULTRALIGHT, UID, atr, bundles, tech);

        assertEquals(tech.size(), bundles.size());
        return tech;
    }
}
