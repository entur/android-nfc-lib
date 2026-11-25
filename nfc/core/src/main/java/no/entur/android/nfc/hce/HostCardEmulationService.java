package no.entur.android.nfc.hce;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;

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

		// enable / disable regardless of whether we are the default service
		this.service = new ComponentName(context, serviceClass);
		this.cardEmulation = getCardEmulation(context);
	}

	private static CardEmulation getCardEmulation(Context context) {
		NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(context);
		if (defaultAdapter == null) {
			return null;
		}
		try {
			return CardEmulation.getInstance(defaultAdapter);
		} catch (UnsupportedOperationException e) {
			// NFC, but no host card emulation
			return null;
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
		if (!isCardEmulation()) {
			return false;
		}
		PackageManager pm = context.getPackageManager();
		int state = pm.getComponentEnabledSetting(service);
		if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
			// not changed, so as in the manifest
			return isEnabledInManifest(pm);
		}
		return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
	}

	@SuppressWarnings("deprecation")
	protected boolean isEnabledInManifest(PackageManager pm) {
		try {
			int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? PackageManager.MATCH_DISABLED_COMPONENTS : PackageManager.GET_DISABLED_COMPONENTS;
			ServiceInfo serviceInfo = pm.getServiceInfo(service, flags);
			return serviceInfo.enabled && serviceInfo.applicationInfo.enabled;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	public boolean isDefault() {
		if (!isCardEmulation()) {
			return false;
		}
		return cardEmulation.isDefaultServiceForAid(service, aid);
	}

}
