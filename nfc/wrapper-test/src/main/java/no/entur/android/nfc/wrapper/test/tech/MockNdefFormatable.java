package no.entur.android.nfc.wrapper.test.tech;

import android.nfc.NdefMessage;

import no.entur.android.nfc.wrapper.tech.BasicTagTechnology;

public class MockNdefFormatable extends MockBasicTagTechnology {
    public MockNdefFormatable() {
        super(BasicTagTechnology.NDEF_FORMATABLE);
    }

    public void format(NdefMessage ndefMessage) {

    }
}
