package no.entur.android.nfc.hce.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import no.entur.android.nfc.hce.HostCardEmulationActivitySupport;

/**
 *
 * Abstract activity for giving priority to a certain HCE service class during this activity's foreground operation, so that other device might call us via HCE.
 *
 */

public abstract class NfcTargetCompatActivity extends AppCompatActivity {

	private static final String TAG = NfcTargetCompatActivity.class.getName();

	protected HostCardEmulationActivitySupport hceSupport;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.hceSupport = createHostCardEmulationActivitySupport();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);

		this.hceSupport = createHostCardEmulationActivitySupport();
	}

	protected abstract HostCardEmulationActivitySupport createHostCardEmulationActivitySupport();

	@Override
	protected void onPause() {
		super.onPause();
		if (hceSupport != null) {
			hceSupport.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (hceSupport != null) {
			hceSupport.onResume();
		}
	}

}
