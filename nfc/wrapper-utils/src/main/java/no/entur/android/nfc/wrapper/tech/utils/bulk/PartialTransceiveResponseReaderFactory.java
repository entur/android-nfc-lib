package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

public interface PartialTransceiveResponseReaderFactory extends Parcelable {

    PartialTransceiveResponseReader create(byte[] command, PartialTransceiveResponsePredicate predicate);

}
