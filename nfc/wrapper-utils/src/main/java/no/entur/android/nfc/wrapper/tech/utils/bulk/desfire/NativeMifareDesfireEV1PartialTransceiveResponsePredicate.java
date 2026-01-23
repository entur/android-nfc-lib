package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

public class NativeMifareDesfireEV1PartialTransceiveResponsePredicate implements PartialTransceiveResponsePredicate {

    public static final int ADDITIONAL_FRAME_STATUS = 0xAF;

    @Override
    public boolean test(byte[] response) {

        if(response == null || response.length == 0) {
            return false;
        }

        return ADDITIONAL_FRAME_STATUS == (response[0] & 0xFF);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<NativeMifareDesfireEV1PartialTransceiveResponsePredicate> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1PartialTransceiveResponsePredicate>() {
        @Override
        public NativeMifareDesfireEV1PartialTransceiveResponsePredicate createFromParcel(Parcel in) {
            return new NativeMifareDesfireEV1PartialTransceiveResponsePredicate();
        }

        @Override
        public NativeMifareDesfireEV1PartialTransceiveResponsePredicate[] newArray(int size) {
            return new NativeMifareDesfireEV1PartialTransceiveResponsePredicate[size];
        }
    };
}
