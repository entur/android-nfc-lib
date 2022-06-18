package no.entur.android.nfc.external.remote;

public class RemoteCommandException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RemoteCommandException() {
		super();
	}

	public RemoteCommandException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RemoteCommandException(String detailMessage) {
		super(detailMessage);
	}

	public RemoteCommandException(Throwable throwable) {
		super(throwable);
	}

}
