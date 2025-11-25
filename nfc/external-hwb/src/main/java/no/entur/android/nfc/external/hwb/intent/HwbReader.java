package no.entur.android.nfc.external.hwb.intent;

import android.os.Parcelable;

import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public abstract class HwbReader extends RemoteCommandReader {

	protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new HwbReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new HwbReaderException(string);
	}

}
