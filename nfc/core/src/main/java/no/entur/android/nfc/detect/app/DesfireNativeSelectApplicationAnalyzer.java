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

    private static boolean isSuccess(byte[] responseApdu) {
        if(responseApdu.length >= 1) {
            int status = responseApdu[responseApdu.length - 1] & 0xFF;

            return status == 0x00;
        }
        return false;
    }
}
