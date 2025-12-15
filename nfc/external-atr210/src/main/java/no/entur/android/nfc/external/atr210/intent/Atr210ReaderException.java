package no.entur.android.nfc.external.atr210.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class Atr210ReaderException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public Atr210ReaderException() {
		super();
	}

	public Atr210ReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public Atr210ReaderException(String detailMessage) {
		super(detailMessage);
	}

	public Atr210ReaderException(Throwable throwable) {
		super(throwable);
	}

}
