package no.entur.android.nfc.hce;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;

/**
 *
 * Enable or disable a HCE service.
 *
 */

public class HostCardEmulationService {

	protected final Context context;
	protected final ComponentName service;

	protected final boolean enforceService;

	public HostCardEmulationService(Context context, String serviceClass, String aid) {
		this.context = context;

		NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(context);
		if (defaultAdapter != null) {

			// enable / disable regardless of whether we are the default service
			this.service = new ComponentName(context, serviceClass);
			this.enforceService = true;
		} else {
			this.service = null;
			this.enforceService = false;
		}
	}

	public void enable() {
		if (enforceService) {
			setMode(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
		}
	}

	public void disable() {
		if (enforceService) {
			setMode(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
		}
	}

	protected void setMode(int mode) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(service, mode, PackageManager.DONT_KILL_APP);
	}
}
