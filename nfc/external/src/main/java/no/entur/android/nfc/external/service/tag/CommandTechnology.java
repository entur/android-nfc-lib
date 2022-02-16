package no.entur.android.nfc.external.service.tag;

import android.os.RemoteException;

import no.entur.android.nfc.wrapper.TransceiveResult;

public interface CommandTechnology extends TagTechnology {

	TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException;

}
