package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

public interface PartialTranscieveResponseReaderFactory extends Parcelable {

    PartialTranscieveResponseReader create(byte[] command, PartialTranscieveResponsePredicate predicate);

}
