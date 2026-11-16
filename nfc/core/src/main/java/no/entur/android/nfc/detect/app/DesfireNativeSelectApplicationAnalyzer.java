package no.entur.android.nfc.detect.app;

import android.content.Intent;

import java.io.IOException;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class DesfireNativeSelectApplicationAnalyzer implements SelectApplicationAnalyzer {

    protected static final byte SELECT_APPLICATION = 0x5A;

    protected final byte[] applicationIdentifier;
    protected final byte[] commandApdu;

    public DesfireNativeSelectApplicationAnalyzer(byte[] applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;

        this.commandApdu = new byte[applicationIdentifier.length + 1];
        this.commandApdu[0] = SELECT_APPLICATION;
        System.arraycopy(applicationIdentifier, 0, commandApdu, 1, applicationIdentifier.length);
    }

    @Override
    public SelectApplicationAnalyzeResult processApplication(TagTechnologies tagTechnologies, Tag tag, Intent intent) throws IOException {

        IsoDep isoDep = tagTechnologies.getIsoDep();
        if(isoDep != null) {
            if(!isoDep.isConnected()) {
                isoDep.connect();
            }

            byte[] responseApdu = isoDep.transceive(commandApdu);

            boolean success = isSuccess(responseApdu);

            return new SelectApplicationAnalyzeResult(success, applicationIdentifier, commandApdu, responseApdu);
        }
        return null;
    }

    static boolean isSuccess(byte[] responseApdu) {
        // the native SelectApplication response is a single status byte (00 for success, i.e. A0 for application not found).
        // A card which only speaks ISO 7816 answers with SW1 SW2 instead, i.e. 6D 00 or 67 00, which must not be read as success.
        return responseApdu != null && responseApdu.length == 1 && responseApdu[0] == 0x00;
    }
}
