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
    protected final byte[] commandAdpu;

    public DesfireNativeSelectApplicationAnalyzer(byte[] applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;

        this.commandAdpu = new byte[applicationIdentifier.length + 1];
        this.commandAdpu[0] = SELECT_APPLICATION;
        System.arraycopy(applicationIdentifier, 0, commandAdpu, 1, applicationIdentifier.length);
    }

    @Override
    public SelectApplicationAnalyzeResult processApplication(TagTechnologies tagTechnologies, Tag tag, Intent intent) throws IOException {

        IsoDep isoDep = tagTechnologies.getIsoDep();
        if(isoDep != null) {
            if(!isoDep.isConnected()) {
                isoDep.connect();
            }

            byte[] responseAdpu = isoDep.transceive(commandAdpu);

            boolean success = isSuccess(responseAdpu);

            return new SelectApplicationAnalyzeResult(success, applicationIdentifier, commandAdpu, responseAdpu);
        }
        return null;
    }

    private static boolean isSuccess(byte[] responseAdpu) {
        if(responseAdpu.length >= 1) {
            int status = responseAdpu[responseAdpu.length - 1] & 0xFF;

            return status == 0x00;
        }
        return false;
    }
}
