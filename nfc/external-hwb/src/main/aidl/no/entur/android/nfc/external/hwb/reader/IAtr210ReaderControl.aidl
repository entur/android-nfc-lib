package no.entur.android.nfc.external.hwb.reader;

interface IAtr210ReaderControl {

    byte[] getConfiguration();

    byte[] getDiagnostics();
}
