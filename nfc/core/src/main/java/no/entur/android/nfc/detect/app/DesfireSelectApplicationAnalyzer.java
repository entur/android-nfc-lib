package no.entur.android.nfc.detect.app;

import android.content.Intent;

import java.io.IOException;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class DesfireSelectApplicationAnalyzer implements SelectApplicationAnalyzer {

    protected static final byte SELECT_APPLICATION = 0x5A;

    protected final byte[] applicationIdentifier;
    protected final byte[] commandAdpu;

    public DesfireSelectApplicationAnalyzer(byte[] applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;

        commandAdpu = new byte[6 + applicationIdentifier.length];
        commandAdpu[0] = (byte) 0x90;
        commandAdpu[1] = (byte) SELECT_APPLICATION;
        commandAdpu[4] = (byte) applicationIdentifier.length;

        System.arraycopy(applicationIdentifier, 0, commandAdpu, 5, applicationIdentifier.length);
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
        if(responseAdpu.length >= 2) {
            int sw1 = responseAdpu[responseAdpu.length - 2] & 0xFF;
            int sw2 = responseAdpu[responseAdpu.length - 1] & 0xFF;

            return sw1 == 0x91 && sw2 == 0x00;
        }
        return false;
    }
}
