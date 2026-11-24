package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.remote.RemoteCommandException;

public class HwbServiceException extends RemoteCommandException {

	private static final long serialVersionUID = 1L;

	public HwbServiceException() {
		super();
	}

	public HwbServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public HwbServiceException(String detailMessage) {
		super(detailMessage);
	}

	public HwbServiceException(Throwable throwable) {
		super(throwable);
	}

}
