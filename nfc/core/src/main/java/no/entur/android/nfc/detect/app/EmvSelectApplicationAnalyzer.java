package no.entur.android.nfc.detect.app;

import android.content.Intent;

import java.io.IOException;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class EmvSelectApplicationAnalyzer implements SelectApplicationAnalyzer {

    // with 2PAY.SYS.DDF01
    protected static final byte[] SELECT_PPSE_COMMAND = new byte[] {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};
    protected final byte[] AID = new byte[] {0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31};

    public EmvSelectApplicationAnalyzer() {
    }

    @Override
    public SelectApplicationAnalyzeResult processApplication(TagTechnologies tagTechnologies, Tag tag, Intent intent) throws IOException {

        IsoDep isoDep = tagTechnologies.getIsoDep();
        if(isoDep != null) {
            if(!isoDep.isConnected()) {
                isoDep.connect();
            }

            byte[] responseApdu = isoDep.transceive(SELECT_PPSE_COMMAND);

            boolean success = isSuccess(responseApdu);

            return new SelectApplicationAnalyzeResult(success, AID, SELECT_PPSE_COMMAND, responseApdu);
        }
        return null;
    }

    private static boolean isSuccess(byte[] responseApdu) {
        if(responseApdu.length >= 2) {
            int sw1 = responseApdu[responseApdu.length - 2] & 0xFF;
            int sw2 = responseApdu[responseApdu.length - 1] & 0xFF;

            return sw1 == 0x90 && sw2 == 0x00;
        }
        return false;
    }

}
