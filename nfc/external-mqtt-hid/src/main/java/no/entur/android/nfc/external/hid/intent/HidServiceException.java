package no.entur.android.nfc.external.hid.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class HidServiceException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public HidServiceException() {
		super();
	}

	public HidServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public HidServiceException(String detailMessage) {
		super(detailMessage);
	}

	public HidServiceException(Throwable throwable) {
		super(throwable);
	}

}
