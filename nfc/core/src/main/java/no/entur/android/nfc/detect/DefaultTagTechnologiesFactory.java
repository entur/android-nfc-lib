package no.entur.android.nfc.detect;

import android.content.Intent;

import java.util.Set;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareClassic;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.NfcB;
import no.entur.android.nfc.wrapper.tech.NfcF;
import no.entur.android.nfc.wrapper.tech.NfcV;

public class DefaultTagTechnologiesFactory implements TagTechnologiesFactory{

    @Override
    public TagTechnologies newInstance(Tag tag, Intent intent, Set<String> technologies) {

        TagTechnologies tech = new TagTechnologies();

        String[] techList = tag.getTechList();

        for (String s : techList) {
            if(technologies.contains(s)) {
                switch(s) {
                    case ISO_DEP: {
                        tech.setIsoDep(IsoDep.get(tag));
                        break;
                    }
                    case MIFARE_CLASSIC: {
                        tech.setMifareClassic(MifareClassic.get(tag));
                        break;
                    }
                    case MIFARE_ULTRALIGHT: {
                        tech.setMifareUltralight(MifareUltralight.get(tag));
                        break;
                    }
                    case NFC_A: {
                        tech.setNfcA(NfcA.get(tag));
                        break;
                    }
                    case NFC_B: {
                        tech.setNfcB(NfcB.get(tag));
                        break;
                    }
                    case NFC_F: {
                        tech.setNfcF(NfcF.get(tag));
                        break;
                    }
                    case NFC_V: {
                        tech.setNfcV(NfcV.get(tag));
                        break;
                    }
                }
            }
        }
        return tech;
    }
}
