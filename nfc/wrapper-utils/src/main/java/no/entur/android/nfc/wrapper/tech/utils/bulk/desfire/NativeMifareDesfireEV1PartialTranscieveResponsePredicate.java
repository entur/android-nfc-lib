package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;

import androidx.annotation.NonNull;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class NativeMifareDesfireEV1PartialTranscieveResponsePredicate implements PartialTranscieveResponsePredicate {

    public static final int ADDITIONAL_FRAME_STATUS = 0xAF;

    @Override
    public boolean test(byte[] response) {

        if(response == null || response.length == 0) {
            return false;
        }

        return ADDITIONAL_FRAME_STATUS == (response[0] & 0xFF);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
