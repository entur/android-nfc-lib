package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class HwbReaderException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public HwbReaderException() {
		super();
	}

	public HwbReaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public HwbReaderException(String detailMessage) {
		super(detailMessage);
	}

	public HwbReaderException(Throwable throwable) {
		super(throwable);
	}

}
