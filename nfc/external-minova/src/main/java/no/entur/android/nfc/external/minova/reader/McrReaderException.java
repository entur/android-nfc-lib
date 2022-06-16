package no.entur.android.nfc.external.minova.reader;

public class McrReaderException extends RuntimeException {

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
