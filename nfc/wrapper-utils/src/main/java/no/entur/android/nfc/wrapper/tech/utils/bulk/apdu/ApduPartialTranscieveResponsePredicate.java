package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class ApduPartialTranscieveResponsePredicate implements PartialTranscieveResponsePredicate {

    protected final int sw1;

    protected final int sw2;

    public ApduPartialTranscieveResponsePredicate() {
        this(0x91, 0xAF);
    }

    public ApduPartialTranscieveResponsePredicate(int sw1, int sw2) {
        this.sw1 = sw1;
        this.sw2 = sw2;
    }

    @Override
    public boolean test(byte[] response) {

        if(response == null || response.length < 2) {
            return false;
        }

        int responseSw1 = response[response.length - 2] & 0xFF;
        if(responseSw1 != sw1) {
            return false;
        }
        int responseSw2 = response[response.length - 1] & 0xFF;

        return sw2 == responseSw2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<ApduPartialTranscieveResponsePredicate> CREATOR = new Parcelable.Creator<ApduPartialTranscieveResponsePredicate>() {
        @Override
        public ApduPartialTranscieveResponsePredicate createFromParcel(Parcel in) {
            return new ApduPartialTranscieveResponsePredicate();
        }

        @Override
        public ApduPartialTranscieveResponsePredicate[] newArray(int size) {
            return new ApduPartialTranscieveResponsePredicate[size];
        }
    };
}
