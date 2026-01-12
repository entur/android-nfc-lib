package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseReader;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseReaderFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory implements PartialTranscieveResponseReaderFactory {

    private static final byte[] ADDITIONAL_FRAME = new byte[] { (byte) 0xAF };

    @Override
    public PartialTranscieveResponseReader create(byte[] command, PartialTranscieveResponsePredicate predicate) {
        return new NativeMifareDesfireEV1PartialTranscieveResponseReader(ADDITIONAL_FRAME, predicate);
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory>() {
        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory createFromParcel(Parcel in) {
            return new NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory();
        }

        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory[] newArray(int size) {
            return new NativeMifareDesfireEV1PartialTranscieveResponseReaderFactory[size];
        }
    };

}
