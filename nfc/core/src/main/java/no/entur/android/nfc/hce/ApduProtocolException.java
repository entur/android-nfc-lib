package no.entur.android.nfc.hce;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class ApduProtocolException extends RuntimeException {

	public ApduProtocolException() {
	}

	public ApduProtocolException(String msg) {
		super(msg);
	}

	public ApduProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApduProtocolException(Throwable cause) {
		super(cause);
	}

	@RequiresApi(Build.VERSION_CODES.N)
	public ApduProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
