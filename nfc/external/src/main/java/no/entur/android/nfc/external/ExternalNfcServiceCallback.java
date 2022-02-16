package no.entur.android.nfc.external;

import android.content.Intent;

public interface ExternalNfcServiceCallback {

	public static final String ACTION_SERVICE_STARTED = ExternalNfcServiceCallback.class.getName() + ".action.SERVICE_STARTED";
	public static final String ACTION_SERVICE_STOPPED = ExternalNfcServiceCallback.class.getName() + ".action.SERVICE_STOPPED";

	void onExternalNfcServiceStopped(Intent intent);

	void onExternalNfcServiceStarted(Intent intent);

}
