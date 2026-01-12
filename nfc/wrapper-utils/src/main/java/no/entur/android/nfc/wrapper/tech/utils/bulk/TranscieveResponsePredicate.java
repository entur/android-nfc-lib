package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

import java.util.function.Predicate;

public interface TranscieveResponsePredicate extends Parcelable {

    boolean test(byte[] response);

}
