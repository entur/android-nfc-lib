package no.entur.android.nfc.websocket.android;

import android.os.Parcelable;
import android.os.RemoteException;

import no.entur.android.nfc.external.service.tag.ReaderTechnology;
import no.entur.android.nfc.wrapper.ErrorCodes;

// this is mostly a dummy implementation, the reader is remote; see also MinovaReaderTechnology

public class WebsocketReaderTechnology implements ReaderTechnology {

    // assumed default value
    protected int maxTransieveLength = 253;

    @Override
    public int setTimeout(int technology, int timeout) throws RemoteException {
        return ErrorCodes.SUCCESS;
    }

    @Override
    public int getTimeout(int technology) throws RemoteException {
        return 0;
    }

    @Override
    public void resetTimeouts() throws RemoteException {
        // ignore
    }

    @Override
    public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        return false;
    }

    @Override
    public int getMaxTransceiveLength(int technology) throws RemoteException {
        return maxTransieveLength;
    }

    @Override
    public boolean getExtendedLengthApdusSupported() throws RemoteException {
        return maxTransieveLength > 256;
    }

    @Override
    public int reconnect(int handle) throws RemoteException {
        return ErrorCodes.SUCCESS;
    }

    @Override
    public Parcelable transceiveMetadata(Parcelable parcelable) {
        return null;
    }
}
