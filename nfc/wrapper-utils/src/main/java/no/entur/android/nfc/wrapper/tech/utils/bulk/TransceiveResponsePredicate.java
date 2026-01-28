package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

/**
 *
 * Predicate for checking the transceive response.
 *
 */

public interface TransceiveResponsePredicate extends Parcelable {

    boolean test(byte[] response);

}
