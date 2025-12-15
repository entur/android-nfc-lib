package no.entur.android.nfc.external.tag.bulk.validate;

import android.os.Parcelable;

public interface TransceiveResponseValidator extends Parcelable {

    boolean isValid(byte[] response);

}
