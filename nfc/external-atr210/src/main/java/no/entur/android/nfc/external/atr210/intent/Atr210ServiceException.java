package no.entur.android.nfc.external.atr210.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class Atr210ServiceException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public Atr210ServiceException() {
		super();
	}

	public Atr210ServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public Atr210ServiceException(String detailMessage) {
		super(detailMessage);
	}

	public Atr210ServiceException(Throwable throwable) {
		super(throwable);
	}

}
