package no.entur.android.nfc.external.atr210.reader;

interface IAtr210ReaderControl {

    byte[] getNfcReadersConfiguration(long timeout);

    byte[] setNfcReadersConfiguration(in byte[] value, long timeout);

    byte[] getNfcReaders(long timeout);

}
