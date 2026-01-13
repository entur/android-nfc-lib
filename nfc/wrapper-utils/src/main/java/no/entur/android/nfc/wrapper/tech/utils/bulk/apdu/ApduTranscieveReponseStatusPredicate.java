package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

/**
 *
 * Predicate to determine whether the card responsed as expected to a command.
 *
 * If negative, the expectation is that the remaining commands are discontinued.
 */

public class ApduTranscieveReponseStatusPredicate implements PartialTranscieveResponsePredicate {

    protected final int sw1;

    protected final int sw2;

    public ApduTranscieveReponseStatusPredicate(int sw1, int sw2) {
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

    public static final Parcelable.Creator<ApduTranscieveReponseStatusPredicate> CREATOR = new Parcelable.Creator<ApduTranscieveReponseStatusPredicate>() {
        @Override
        public ApduTranscieveReponseStatusPredicate createFromParcel(Parcel in) {

            int sw1 = in.readInt();
            int sw2 = in.readInt();

            return new ApduTranscieveReponseStatusPredicate(sw1, sw2);
        }

        @Override
        public ApduTranscieveReponseStatusPredicate[] newArray(int size) {
            return new ApduTranscieveReponseStatusPredicate[size];
        }
    };
}
