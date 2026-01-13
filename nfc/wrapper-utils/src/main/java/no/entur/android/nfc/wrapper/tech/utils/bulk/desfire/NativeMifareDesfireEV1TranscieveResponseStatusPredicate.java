package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

/**
 *
 * Predicate to determine whether the card responsed as expected to a command.
 *
 * If negative, the expectation is that the remaining commands are discontinued.
 */

public class NativeMifareDesfireEV1TranscieveResponseStatusPredicate implements PartialTranscieveResponsePredicate {

    protected final int status;

    public NativeMifareDesfireEV1TranscieveResponseStatusPredicate(int status) {
        this.status = status;
    }

    @Override
    public boolean test(byte[] response) {

        if(response == null || response.length == 0) {
            return false;
        }

        int responseStatus = response[0] & 0xFF;

        return responseStatus == status;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    public static final Parcelable.Creator<NativeMifareDesfireEV1TranscieveResponseStatusPredicate> CREATOR = new Parcelable.Creator<NativeMifareDesfireEV1TranscieveResponseStatusPredicate>() {
        @Override
        public NativeMifareDesfireEV1TranscieveResponseStatusPredicate createFromParcel(Parcel in) {

            int status = in.readInt();

            return new NativeMifareDesfireEV1TranscieveResponseStatusPredicate(status);
        }

        @Override
        public NativeMifareDesfireEV1TranscieveResponseStatusPredicate[] newArray(int size) {
            return new NativeMifareDesfireEV1TranscieveResponseStatusPredicate[size];
        }
    };


}
