package no.entur.android.nfc.detect;

import android.content.Intent;

import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private int isoDepTimeout = -1;
        private int mifareClassicTimeout = -1;
        private int mifareUltralightTimeout = -1;
        private int nfcATimeout = -1;
        private int nfcFTimeout = -1;

        public Builder withIsoDepTimeout(int isoDepTimeout) {
            this.isoDepTimeout = isoDepTimeout;
            return this;
        }

        public Builder withMifareClassicTimeout(int mifareClassicTimeout) {
            this.mifareClassicTimeout = mifareClassicTimeout;
            return this;
        }

        public Builder withMifareUltralightTimeout(int mifareUltralightTimeout) {
            this.mifareUltralightTimeout = mifareUltralightTimeout;
            return this;
        }

        public Builder withNfcATimeout(int nfcATimeout) {
            this.nfcATimeout = nfcATimeout;
            return this;
        }

        public Builder withNfcFTimeout(int nfcFTimeout) {
            this.nfcFTimeout = nfcFTimeout;
            return this;
        }

        public DefaultTagTechnologiesFactory build() {
            return new DefaultTagTechnologiesFactory(isoDepTimeout, mifareClassicTimeout, mifareUltralightTimeout, nfcATimeout, nfcFTimeout);
        }

    }

    private final int isoDepTimeout;
    private final int mifareClassicTimeout;
    private final int mifareUltralightTimeout;
    private final int nfcATimeout;
    private final int nfcFTimeout;

    public DefaultTagTechnologiesFactory() {
        this(-1, -1, -1, -1, -1);
    }
    public DefaultTagTechnologiesFactory(int isoDepTimeout, int mifareClassicTimeout, int mifareUltralightTimeout, int nfcATimeout, int nfcFTimeout) {
        this.isoDepTimeout = isoDepTimeout;
        this.mifareClassicTimeout = mifareClassicTimeout;
        this.mifareUltralightTimeout = mifareUltralightTimeout;
        this.nfcATimeout = nfcATimeout;
        this.nfcFTimeout = nfcFTimeout;
    }

    @Override
    public TagTechnologies newInstance(Tag tag, Intent intent, Set<String> technologies) {

        TagTechnologies tech = new TagTechnologies();

        String[] techList = tag.getTechList();

        for (String s : techList) {
            if(technologies.contains(s)) {
                switch(s) {
                    case ISO_DEP: {
                        IsoDep isoDep = IsoDep.get(tag);
                        if(isoDepTimeout != -1) {
                            isoDep.setTimeout(isoDepTimeout);
                        }
                        tech.setIsoDep(isoDep);
                        break;
                    }
                    case MIFARE_CLASSIC: {
                        MifareClassic mifareClassic = MifareClassic.get(tag);
                        if(mifareClassicTimeout != -1) {
                            mifareClassic.setTimeout(mifareClassicTimeout);
                        }
                        tech.setMifareClassic(mifareClassic);
                        break;
                    }
                    case MIFARE_ULTRALIGHT: {
                        MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                        if(mifareUltralightTimeout != -1) {
                            mifareUltralight.setTimeout(mifareUltralightTimeout);
                        }
                        tech.setMifareUltralight(mifareUltralight);
                        break;
                    }
                    case NFC_A: {
                        NfcA nfcA = NfcA.get(tag);
                        if(nfcATimeout != -1) {
                            nfcA.setTimeout(nfcATimeout);
                        }
                        tech.setNfcA(nfcA);
                        break;
                    }
                    case NFC_B: {
                        NfcB nfcB = NfcB.get(tag);
                        // no timeout
                        tech.setNfcB(nfcB);
                        break;
                    }
                    case NFC_F: {
                        NfcF nfcF = NfcF.get(tag);
                        if(nfcFTimeout != -1) {
                            nfcF.setTimeout(nfcFTimeout);
                        }
                        tech.setNfcF(nfcF);
                        break;
                    }
                    case NFC_V: {
                        NfcV nfcV = NfcV.get(tag);
                        // no timeout
                        tech.setNfcV(nfcV);
                        break;
                    }
                }
            }
        }
        return tech;
    }
}
