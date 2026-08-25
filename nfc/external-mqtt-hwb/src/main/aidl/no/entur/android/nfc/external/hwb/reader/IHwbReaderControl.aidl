package no.entur.android.nfc.external.hwb.reader;

interface IHwbReaderControl {

    byte[] getDiagnostics(long timeout);

    byte[] isPresent(long timeout);

    byte[] setControlResult(String validity, String title, String description);

}
