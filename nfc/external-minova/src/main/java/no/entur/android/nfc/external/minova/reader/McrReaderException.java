package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class McrReaderException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public McrReaderException() {
		super();
	}

	public McrReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public McrReaderException(String detailMessage) {
		super(detailMessage);
	}

	public McrReaderException(Throwable throwable) {
		super(throwable);
	}

}
