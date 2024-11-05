package no.entur.android.nfc.detect;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;

public interface MifareUltralightHandler {

    void onMifareUltralightDiscovered(MifareUltralight mifareUltralight, Tag tag, Intent intent);

}
