package no.entur.android.nfc.wrapper.test.tech.transceive;

import android.os.Parcelable;

import java.io.IOException;

public interface MockParcelableTransceive extends MockTransceive {

    <T> T parcelableTranscieve(Parcelable data) throws IOException;

    Parcelable parcelableTransceiveMetadata(Parcelable data);
}
