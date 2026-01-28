package no.entur.android.nfc.wrapper.tech;

import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveMetadata;
import no.entur.android.nfc.wrapper.ParcelableTransceiveMetadataResult;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;
import no.entur.android.nfc.wrapper.TagImpl;

/**
 *
 * Extended functionality for talking for tags.
 *
 * Primarily for talking to networked readers; sending multiple commands and/or adding tracing and so on.
 *
 */

public class ExtendedBasicTagTechnologyImpl extends BasicTagTechnologyImpl {
    ExtendedBasicTagTechnologyImpl(TagImpl tag, int tech) throws RemoteException {
        super(tag, tech);
    }

    public Parcelable transceive(Parcelable command) throws IOException {
        checkConnected();

        try {
            ParcelableTransceive transceive = new ParcelableTransceive(command);

            ParcelableTransceiveResult result = mTag.getTagService().parcelableTranscieve(mTag.getServiceHandle(), transceive);
            if (result == null) {
                throw new IOException("transceive failed");
            } else {
                return result.getResponseOrThrow();
            }
        } catch (RemoteException e) {
            Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
            throw new IOException(NFC_SERVICE_DEAD_MSG);
        }
    }

    public Parcelable transceiveMetadata(Parcelable metadata) throws IOException {
        try {
            ParcelableTransceiveMetadata transceive = new ParcelableTransceiveMetadata(metadata);

            ParcelableTransceiveMetadataResult result = mTag.getTagService().parcelableTransceiveMetadata(transceive);
            if (result == null) {
                throw new IOException("transceive failed");
            }

            return result.getResponseData();
        } catch (RemoteException e) {
            Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
            throw new IOException(NFC_SERVICE_DEAD_MSG);
        }
    }
}
