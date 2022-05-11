package no.entur.android.nfc.external.service.tag;

import android.nfc.NdefMessage;

public interface NdefTechnology extends TagTechnology {
    
    int ndefMakeReadOnly() ;

    NdefMessage ndefRead();

    int ndefWrite(NdefMessage msg);
}
