package no.entur.android.nfc.wrapper.tech.utils.bulk;

public interface PartialTransceiveResponseReader {

    byte[] next(byte[] content);

    byte[] assemble();

}
