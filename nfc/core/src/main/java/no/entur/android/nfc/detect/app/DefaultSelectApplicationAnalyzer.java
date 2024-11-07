package no.entur.android.nfc.detect.app;

import android.content.Intent;

import java.io.IOException;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class DefaultSelectApplicationAnalyzer implements SelectApplicationAnalyzer {

    protected final byte[] applicationIdentifier;
    protected final byte[] commandAdpu;

    public DefaultSelectApplicationAnalyzer(byte[] bytes) {
        this.applicationIdentifier = bytes;

        commandAdpu = buildSelectApplicationCommand(bytes);
    }

    private byte[] buildSelectApplicationCommand(byte[] bytes) {
        byte[] command = new byte[6 + bytes.length];
        command[0] = (byte) 0x00; // CLA
        command[1] = (byte) 0xA4; // INS
        command[2] = (byte) 0x04; // P1
        // 3: 0x00 P2
        // 4: payload length
        // 5...n-1 : application id
        // n: Lc
        command[4] = (byte) bytes.length;

        System.arraycopy(bytes, 0, command, 5, bytes.length);

        return command;
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
