package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

/**
 *
 * Predicate to determine whether the card responsed as expected to a command.
 *
 * If negative, the expectation is that the remaining commands are discontinued.
 */

public class ApduTransceiveReponseStatusPredicate implements PartialTransceiveResponsePredicate {

    protected final int sw1;

    protected final int sw2;

    public ApduTransceiveReponseStatusPredicate(int sw1, int sw2) {
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
        dest.writeInt(sw1);
        dest.writeInt(sw2);
    }

    public static final Parcelable.Creator<ApduTransceiveReponseStatusPredicate> CREATOR = new Parcelable.Creator<ApduTransceiveReponseStatusPredicate>() {
        @Override
        public ApduTransceiveReponseStatusPredicate createFromParcel(Parcel in) {

            int sw1 = in.readInt();
            int sw2 = in.readInt();

            return new ApduTransceiveReponseStatusPredicate(sw1, sw2);
        }

        @Override
        public ApduTransceiveReponseStatusPredicate[] newArray(int size) {
            return new ApduTransceiveReponseStatusPredicate[size];
        }
    };

    public int getSw1() {
        return sw1;
    }

    public int getSw2() {
        return sw2;
    }
}
