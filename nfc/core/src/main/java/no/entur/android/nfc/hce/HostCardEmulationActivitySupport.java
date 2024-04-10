package no.entur.android.nfc.hce;

import android.app.Activity;
import android.content.ComponentName;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.HostApduService;

import no.entur.android.nfc.AbstractActivitySupport;

/**
 *
 * Make our HCE the preferred one as the activity is in the foreground.
 *
 * While HCE might be always-on, multiple applications can be registered on the same application id. If so, a single application will always receive the
 * resulting traffic, or a 'chooser' function might be presented to the user. We want our HCE service to be the one selected while the app is open.
 *
 * Note: only a single HCE service can be preferred at a time.
 */

public class HostCardEmulationActivitySupport extends AbstractActivitySupport {

	private static final String TAG = HostCardEmulationActivitySupport.class.getName();

	protected final Activity activity;
	protected final ComponentName service;

	protected final CardEmulation cardEmulation;

	public HostCardEmulationActivitySupport(Activity activity, Class<? extends HostApduService> serviceClass) {
		this.activity = activity;

		NfcAdapter defaultAdapter = NfcAdapter.getDefaultAdapter(activity);
		if (defaultAdapter != null) {

			this.cardEmulation = CardEmulation.getInstance(defaultAdapter);
			this.service = new ComponentName(activity, serviceClass);
		} else {
			this.cardEmulation = null;
			this.service = null;
		}
	}

	@Override
	protected boolean canStart() {
		return super.canStart() && cardEmulation != null;
	}

	@Override
	protected boolean canStop() {
		return super.canStop() && cardEmulation != null;
	}

	@Override
	protected void stopImpl() {
		if(cardEmulation != null) {
			cardEmulation.unsetPreferredService(activity);
		}
	}

	@Override
	protected void startImpl() {
		if(cardEmulation != null) {
			cardEmulation.setPreferredService(activity, service);
		}
	}
}
