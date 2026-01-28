package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseReader;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseReaderFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

public class NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory implements PartialTransceiveResponseReaderFactory {

    private static final byte[] ADDITIONAL_FRAME = new byte[] { (byte) 0xAF };

    @Override
    public PartialTransceiveResponseReader create(byte[] command, PartialTransceiveResponsePredicate predicate) {
        return new NativeMifareDesfireEV1PartialTransceiveResponseReader(ADDITIONAL_FRAME, predicate);
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory>() {
        @Override
        public NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory createFromParcel(Parcel in) {
            return new NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory();
        }

        @Override
        public NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory[] newArray(int size) {
            return new NativeMifareDesfireEV1PartialTransceiveResponseReaderFactory[size];
        }
    };

}
