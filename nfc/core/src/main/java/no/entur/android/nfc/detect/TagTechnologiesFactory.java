package no.entur.android.nfc.detect;

import android.content.Intent;

import java.util.Set;

import no.entur.android.nfc.wrapper.Tag;

public interface TagTechnologiesFactory {

    String ISO_DEP = "android.nfc.tech.IsoDep";
    String MIFARE_CLASSIC = "android.nfc.tech.MifareClassic";
    String MIFARE_ULTRALIGHT = "android.nfc.tech.MifareUltralight";
    String NFC_A = "android.nfc.tech.NfcA";
    String NFC_B = "android.nfc.tech.NfcB";
    String NFC_F = "android.nfc.tech.NfcF";
    String NFC_V = "android.nfc.tech.NfcV";

    TagTechnologies newInstance(Tag tag, Intent intent, Set<String> technologies);
}
