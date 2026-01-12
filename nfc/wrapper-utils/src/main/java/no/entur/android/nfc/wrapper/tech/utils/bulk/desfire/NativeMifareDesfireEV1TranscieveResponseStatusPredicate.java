package no.entur.android.nfc.wrapper.tech.utils.bulk.desfire;

import android.os.Parcel;

import androidx.annotation.NonNull;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

/**
 *
 * Predicate to determine whether the card responsed as expected to a command.
 *
 * If negative, the expectation is that the remaining commands are discontinued.
 */

public class NativeMifareDesfireEV1TranscieveResponseStatusPredicate implements PartialTranscieveResponsePredicate {

    private int status;

    @Override
    public boolean test(byte[] response) {

        if(response == null || response.length == 0) {
            return false;
        }

        int responseStatus = response[0] & 0xFF;

        return responseStatus == status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}
