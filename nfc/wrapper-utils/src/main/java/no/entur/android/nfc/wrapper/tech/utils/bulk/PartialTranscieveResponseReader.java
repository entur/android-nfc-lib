package no.entur.android.nfc.wrapper.tech.utils.bulk;

public interface PartialTranscieveResponseReader {

    byte[] next(byte[] content);

    byte[] assemble();

}
