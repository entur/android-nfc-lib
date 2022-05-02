package no.entur.android.nfc.external.service.tag;

import android.nfc.NdefMessage;

import no.entur.android.nfc.wrapper.tech.TagTechnology;

public interface NdefTechnology extends TagTechnology {
    
    int ndefMakeReadOnly();

    NdefMessage ndefRead();

    int ndefWrite(NdefMessage msg);
}
