package no.entur.android.nfc.external.hid.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class HidReaderException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public HidReaderException() {
		super();
	}

	public HidReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public HidReaderException(String detailMessage) {
		super(detailMessage);
	}

	public HidReaderException(Throwable throwable) {
		super(throwable);
	}

}
