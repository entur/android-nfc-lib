package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

public class ApduPartialTransceiveResponsePredicate implements PartialTransceiveResponsePredicate {

    protected final int sw1;

    protected final int sw2;

    public ApduPartialTransceiveResponsePredicate() {
        this(0x91, 0xAF);
    }

    public ApduPartialTransceiveResponsePredicate(int sw1, int sw2) {
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

    public static final Parcelable.Creator<ApduPartialTransceiveResponsePredicate> CREATOR = new Parcelable.Creator<ApduPartialTransceiveResponsePredicate>() {
        @Override
        public ApduPartialTransceiveResponsePredicate createFromParcel(Parcel in) {
            return new ApduPartialTransceiveResponsePredicate();
        }

        @Override
        public ApduPartialTransceiveResponsePredicate[] newArray(int size) {
            return new ApduPartialTransceiveResponsePredicate[size];
        }
    };

    public int getSw1() {
        return sw1;
    }

    public int getSw2() {
        return sw2;
    }
}
