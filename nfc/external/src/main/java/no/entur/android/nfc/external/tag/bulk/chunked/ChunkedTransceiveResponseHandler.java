package no.entur.android.nfc.external.tag.bulk.chunked;

import android.os.Parcelable;

public interface ChunkedTransceiveResponseHandler extends Parcelable {

    boolean isChunked(byte[] response);

    byte[] nextChunkedCommandAdpu();

}
