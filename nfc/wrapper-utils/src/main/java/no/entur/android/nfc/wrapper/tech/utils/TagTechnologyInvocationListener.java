package no.entur.android.nfc.wrapper.tech.utils;

public interface TagTechnologyInvocationListener {

    ConnectInvocation onConnect();
    CloseInvocation onClose();

    TransceiveInvocation onTransceive(byte[] command);
    SetTimeoutInvocation onSetTimeout(int value);

}
