package no.entur.android.nfc.external.minova.service;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;

public class MinovaTagType {

    private byte[] historicalBytes;

    private int sak;

    private byte[] atq;

    private byte[] ats;

    private TagType tagType;

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public byte[] getHistoricalBytes() {
        return historicalBytes;
    }

    public void setHistoricalBytes(byte[] historicalBytes) {
        this.historicalBytes = historicalBytes;
    }

    public boolean hasHistoricalBytes() {
        return historicalBytes != null;
    }

    public int getSak() {
        return sak;
    }

    public void setSak(int sak) {
        this.sak = sak;
    }

    public byte[] getAtq() {
        return atq;
    }

    public void setAtq(byte[] atq) {
        this.atq = atq;
    }

    public byte[] getAts() {
        return ats;
    }

    public void setAts(byte[] ats) {
        this.ats = ats;
    }
}
