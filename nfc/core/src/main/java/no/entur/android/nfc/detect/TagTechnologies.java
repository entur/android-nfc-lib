package no.entur.android.nfc.detect;

import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareClassic;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.NfcB;
import no.entur.android.nfc.wrapper.tech.NfcF;
import no.entur.android.nfc.wrapper.tech.NfcV;

public class TagTechnologies {

    private IsoDep isoDep;
    private MifareUltralight mifareUltralight;
    private MifareClassic mifareClassic;

    private NfcA nfcA;
    private NfcB nfcB;
    private NfcF nfcF;
    private NfcV nfcV;

    public IsoDep getIsoDep() {
        return isoDep;
    }

    public void setIsoDep(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    public MifareUltralight getMifareUltralight() {
        return mifareUltralight;
    }

    public void setMifareUltralight(MifareUltralight mifareUltralight) {
        this.mifareUltralight = mifareUltralight;
    }

    public MifareClassic getMifareClassic() {
        return mifareClassic;
    }

    public void setMifareClassic(MifareClassic mifareClassic) {
        this.mifareClassic = mifareClassic;
    }

    public NfcA getNfcA() {
        return nfcA;
    }

    public void setNfcA(NfcA nfcA) {
        this.nfcA = nfcA;
    }

    public NfcB getNfcB() {
        return nfcB;
    }

    public void setNfcB(NfcB nfcB) {
        this.nfcB = nfcB;
    }

    public NfcF getNfcF() {
        return nfcF;
    }

    public void setNfcF(NfcF nfcF) {
        this.nfcF = nfcF;
    }

    public NfcV getNfcV() {
        return nfcV;
    }

    public void setNfcV(NfcV nfcV) {
        this.nfcV = nfcV;
    }

    public boolean hasIsoDep() {
        return isoDep != null;
    }

    public boolean hasNfcA() {
        return nfcA != null;
    }

    public boolean isEmpty() {
        return isoDep == null && mifareUltralight == null && mifareClassic == null && nfcA == null && nfcB == null && nfcF == null && nfcV == null;
    }
}
