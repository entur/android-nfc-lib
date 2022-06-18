package no.entur.android.nfc.external.acs.reader;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class AcrReaderException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public AcrReaderException() {
		super();
	}

	public AcrReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public AcrReaderException(String detailMessage) {
		super(detailMessage);
	}

	public AcrReaderException(Throwable throwable) {
		super(throwable);
	}

}
