package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseHandler;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseHandlerFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory implements PartialTranscieveResponseHandlerFactory {

    private static final byte[] ADDITIONAL_FRAME = new byte[] { (byte) 0xAF };

    @Override
    public PartialTranscieveResponseHandler create(byte[] command, PartialTranscieveResponsePredicate predicate) {
        return new NativeMifareDesfireEV1PartialTranscieveResponseHandler(ADDITIONAL_FRAME, predicate);
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory>() {
        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory createFromParcel(Parcel in) {
            return new NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory();
        }

        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory[] newArray(int size) {
            return new NativeMifareDesfireEV1PartialTranscieveResponseHandlerFactory[size];
        }
    };

}
