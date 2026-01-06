package no.entur.android.nfc.external.hid.reader;

interface IAtr210ReaderControl {

    byte[] getNfcReadersConfiguration(long timeout);

    byte[] setNfcReadersConfiguration(in byte[] value, long timeout);

    byte[] getNfcReaders(long timeout);

    byte[] setResult(boolean valid, String led, String sound);

}
