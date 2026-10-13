package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.Parcelable;
import android.os.RemoteException;

import java.util.Arrays;
import java.util.List;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.external.service.tag.ReaderTechnology;
import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.tech.utils.bulk.metadata.BulkTransceiveMetadata;
import no.entur.android.nfc.wrapper.tech.utils.bulk.metadata.CommandMetadataRequest;
import no.entur.android.nfc.wrapper.tech.utils.bulk.metadata.CommandMetadataResponse;

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
    public Parcelable transceiveMetadata(Parcelable parcelable) {
        if(parcelable instanceof CommandMetadataRequest) {
            List<String> formats = Arrays.asList(CommandMetadataResponse.COMMAND_FORMAT_ISO7816, CommandMetadataResponse.COMMAND_FORMAT_NATIVE_MIFARE_DESFIRE);
            if(supportsCommandTrain) {
                return new CommandMetadataResponse(formats, new BulkTransceiveMetadata(true, true));
            }
            return new CommandMetadataResponse(formats);
        }
        return null;
    }
}
