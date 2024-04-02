package no.entur.android.nfc.external.acs.service;

import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;

public class WrappedAcrReader {

    private final ReaderWrapper readerWrapper;
    private final AcrReader acrReader;

    public WrappedAcrReader(ReaderWrapper readerWrapper, AcrReader acrReader) {
        this.readerWrapper = readerWrapper;
        this.acrReader = acrReader;
    }

    public AcrReader getAcrReader() {
        return acrReader;
    }

    public ReaderWrapper getReaderWrapper() {
        return readerWrapper;
    }
}
