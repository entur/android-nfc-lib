package no.entur.android.nfc.external.test.tech;

import android.nfc.NdefMessage;

import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNdef extends MockBasicTagTechnology {

    private boolean writeable;
    private NdefMessage ndefMessage;

    public MockNdef() {
        super(BasicTagTechnology.NDEF);
    }

    public boolean isWritable() {
        return writeable;
    }

    public boolean makeReadOnly() {
        writeable = false;
        return true;
    }

    public NdefMessage getNdefMessage() {
        return ndefMessage;
    }

    public void writeNdefMessage(NdefMessage ndefMessage) {
        this.ndefMessage = ndefMessage;
    }

    public boolean canMakeReadOnly() {
        return true;
    }
}
