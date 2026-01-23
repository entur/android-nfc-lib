package no.entur.android.nfc.external.service.tag;

import android.os.Parcelable;
import android.os.RemoteException;

public interface ReaderTechnology {

	int setTimeout(int technology, int timeout) throws RemoteException;

	int getTimeout(int technology) throws RemoteException;

	void resetTimeouts() throws RemoteException;

	boolean canMakeReadOnly(int ndefType) throws RemoteException;

	int getMaxTransceiveLength(int technology) throws RemoteException;

	boolean getExtendedLengthApdusSupported() throws RemoteException;

	int reconnect(int handle) throws RemoteException;

    Parcelable transceiveMetadata(Parcelable parcelable);
}
