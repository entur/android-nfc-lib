package no.entur.android.nfc.wrapper.tech.utils.bulk;

public interface PartialTranscieveResponseHandler {

    byte[] next(byte[] content);

    byte[] assemble();

}
