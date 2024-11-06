package no.entur.android.nfc.detect.app;

import android.content.Intent;

import java.io.IOException;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class DefaultSelectApplicationAnalyzer implements SelectApplicationAnalyzer {

    public final static int SELECT_APPLICATION_COMMAND = 0xA4;//

    protected final byte[] applicationIdentifier;
    protected final byte[] commandAdpu;

    public DefaultSelectApplicationAnalyzer(byte[] applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;

        this.commandAdpu = new CommandAPDU(0x00, SELECT_APPLICATION_COMMAND, 0x04, 0x00, applicationIdentifier).getBytes();
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

            return sw1 == 0x90 && sw2 == 0x00;
        }
        return false;
    }
}
