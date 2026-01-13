package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

/**
 *
 * Predicate for checking the transcieve response.
 *
 */

public interface TranscieveResponsePredicate extends Parcelable {

    boolean test(byte[] response);

}
