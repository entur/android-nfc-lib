package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public abstract class Atr210Reader extends RemoteCommandReader {

	protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new Atr210ReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new Atr210ReaderException(string);
	}

}
