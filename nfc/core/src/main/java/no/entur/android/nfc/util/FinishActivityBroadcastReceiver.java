package no.entur.android.nfc.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 *
 * Wrapper for closing dialogs upon receiving NFC traffic (incoming to the HCE service)
 *
 */

public class FinishActivityBroadcastReceiver extends BroadcastReceiver {

	private boolean receiving = false;
	private final Activity activity;
	private final String[] actions;

	public FinishActivityBroadcastReceiver(Activity activity, String... actions) {
		this.activity = activity;
		this.actions = actions;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				activity.finish();
			}
		});
	}

	public void registerReceiver() {
		if (!receiving) {
			IntentFilter serviceFilter = new IntentFilter();
			for (String action : actions) {
				serviceFilter.addAction(action);
			}
			activity.registerReceiver(this, serviceFilter);

			receiving = true;
		}
	}

	public void unregisterReceiver() {
		if (receiving) {
			activity.unregisterReceiver(this);

			receiving = false;
		}
	}
}
