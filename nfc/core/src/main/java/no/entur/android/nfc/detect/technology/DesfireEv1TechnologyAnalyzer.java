package no.entur.android.nfc.detect.technology;

import android.content.Intent;

import no.entur.android.nfc.detect.TagTechnologies;
import no.entur.android.nfc.detect.TagTechnologiesFactory;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class DesfireEv1TechnologyAnalyzer implements TechnologyAnalyzer {
    @Override
    public TechnologyAnalyzeResult processTechnology(TagTechnologies tagTechnologies, Tag tag, Intent intent) {

        // https://stackoverflow.com/questions/11657028/android-nfc-read-atr-from-smartcard-emv
        if(tagTechnologies.hasIsoDep() && tagTechnologies.hasNfcA()) {
            IsoDep isoDep = tagTechnologies.getIsoDep();
            byte[] historicalBytes = isoDep.getHistoricalBytes();
            if(isDesfireEv1(historicalBytes)) {
                return new TechnologyAnalyzeResult("desfireEv1");
            }
        }

        return new TechnologyAnalyzeResult(null);
    }

    @Override
    public String[] getTechnologies() {
        return new String[]{TagTechnologiesFactory.ISO_DEP, TagTechnologiesFactory.NFC_A};
    }

    public static boolean isDesfireEv1(byte[] historicalBytes) {
        if(historicalBytes == null) {
            return false;
        }
        if (historicalBytes.length != 1) {
            return false;
        }
        if (historicalBytes[0] != (byte) 0x80) {
            return false;
        }
        return true;
    }
}
