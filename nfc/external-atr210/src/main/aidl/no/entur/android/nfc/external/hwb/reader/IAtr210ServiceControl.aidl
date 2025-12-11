package no.entur.android.nfc.external.hwb.reader;

interface IAtr210ServiceControl {

    byte[] discoverReaders();

    byte[] getReaderIds();

}
