package no.entur.android.nfc.wrapper.test.tech.transceive;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;

public interface MockParcelableTransceive {

    <T> T parcelableTranscieve(Parcelable data) throws IOException;
    boolean supportsTransceiveParcelable(String className) throws IOException;
}
