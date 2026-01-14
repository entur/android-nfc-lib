package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.external.service.tag.ReaderTechnology;
import no.entur.android.nfc.wrapper.ErrorCodes;

// this is mostly a dummy implementation
// there is a bit of api disconnect between a service with one reader and a service with multiple readers.

public class HwbReaderTechnology implements ReaderTechnology {

    // assumed default value
    protected int maxTransieveLength = 65535;
    protected boolean supportsCommandTrain;

    public HwbReaderTechnology(boolean supportsCommandTrain) {
        this.supportsCommandTrain = supportsCommandTrain;
    }

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
        return 0;
    }

    @Override
    public boolean supportsTransceiveParcelable(String className) {
        return supportsCommandTrain && className.equals(TransmitSchema.class.getName());
    }
}
