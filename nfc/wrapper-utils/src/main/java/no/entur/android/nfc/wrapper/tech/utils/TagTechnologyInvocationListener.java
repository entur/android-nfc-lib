package no.entur.android.nfc.wrapper.tech.utils;

import android.os.Parcelable;

public interface TagTechnologyInvocationListener {

    ConnectInvocation onConnect();

    CloseInvocation onClose();

    TransceiveInvocation onTransceive(byte[] command);

    SetTimeoutInvocation onSetTimeout(int value);

    ParcelableTransceiveInvocation onParcelableTransceive(Parcelable command);

}
