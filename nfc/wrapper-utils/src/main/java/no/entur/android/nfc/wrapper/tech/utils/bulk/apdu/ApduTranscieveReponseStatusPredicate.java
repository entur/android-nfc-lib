package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;

import androidx.annotation.NonNull;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

/**
 *
 * Predicate to determine whether the card responsed as expected to a command.
 *
 * If negative, the expectation is that the remaining commands are discontinued.
 */

public class ApduTranscieveReponseStatusPredicate implements PartialTranscieveResponsePredicate {

    private int sw1;

    private int sw2;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
