package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.apdu.ApduTranscieveReponseStatusPredicate;

public class NativeMifareDesfireEV1PartialTranscieveResponsePredicate implements PartialTranscieveResponsePredicate {

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

    public static final Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponsePredicate> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1PartialTranscieveResponsePredicate>() {
        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponsePredicate createFromParcel(Parcel in) {
            return new NativeMifareDesfireEV1PartialTranscieveResponsePredicate();
        }

        @Override
        public NativeMifareDesfireEV1PartialTranscieveResponsePredicate[] newArray(int size) {
            return new NativeMifareDesfireEV1PartialTranscieveResponsePredicate[size];
        }
    };
}
