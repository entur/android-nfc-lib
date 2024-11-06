package no.entur.android.nfc.detect;

import android.content.Intent;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public interface IsoDepAnalyzeResultHandler {

    void onIsoDepDiscovered(IsoDep isoDep, Tag tag, Intent intent, NfcTargetAnalyzeResult result);

}
