package no.entur.android.nfc.external.minova.reader;

import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import no.entur.android.nfc.external.remote.RemoteCommandReader;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public abstract class MinovaReader extends RemoteCommandReader implements Parcelable {
    private static final String TAG = MinovaReader.class.getName();
}
