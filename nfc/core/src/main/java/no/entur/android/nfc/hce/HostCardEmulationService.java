package no.entur.android.nfc.hce;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;

/**
 *
 * Enable or disable a HCE service.
 *
 */

public class HostCardEmulationService {

	protected final Context context;
	protected final ComponentName service;

	protected final String aid;
	protected final CardEmulation cardEmulation;

	public HostCardEmulationService(Context context, Class serviceClass, String aid) {
		this.context = context;
		this.aid = aid;

		NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(context);
		if (defaultAdapter != null) {
			// enable / disable regardless of whether we are the default service
			this.service = new ComponentName(context, serviceClass);
			this.cardEmulation = CardEmulation.getInstance(defaultAdapter);
		} else {
			this.service = null;
			this.cardEmulation = null;
		}
	}

	public boolean isCardEmulation() {
		return cardEmulation != null;
	}

	public void enable() {
		if (isCardEmulation()) {
			setMode(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
		}
	}

	public void disable() {
		if (isCardEmulation()) {
			setMode(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
		}
	}

	protected void setMode(int mode) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(service, mode, PackageManager.DONT_KILL_APP);
	}

	public String getAid() {
		return aid;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			enable();
		} else {
			disable();
		}
	}

	public boolean isEnabled() {
		PackageManager pm = context.getPackageManager();
		return pm.getComponentEnabledSetting(service) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
	}

	public boolean isDefault() {
		return cardEmulation.isDefaultServiceForAid(service, aid);
	}

}
