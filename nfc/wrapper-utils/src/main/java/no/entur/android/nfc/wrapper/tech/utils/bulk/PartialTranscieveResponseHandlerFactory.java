package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcelable;

public interface PartialTranscieveResponseHandlerFactory extends Parcelable {

    PartialTranscieveResponseHandler create(byte[] command, PartialTranscieveResponsePredicate predicate);

}
