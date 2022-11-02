package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import no.entur.android.nfc.wrapper.Tag;

public class ExternalNfcTagCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcTagCallbackSupport.class);

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcTagCallback callback;
	protected final Activity activity;

	private boolean recieveTagBroadcasts = false;

	protected boolean enabled = false;
	protected Executor executor; // non-final for testing

	private final BroadcastReceiver tagReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			if(executor != null) {
				executor.execute(() -> {
					callback.onExternalTagDiscovered(tag, intent);
				});
			} else {
				callback.onExternalTagDiscovered(tag, intent);
			}

		} else {
			LOGGER.debug("Ignore action " + intent.getAction());
		}
		}
	};

	public ExternalNfcTagCallbackSupport(ExternalNfcTagCallback callback, Activity activity, Executor executor) {
		this.callback = callback;
		this.activity = activity;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onResume() {
		if (enabled) {
			startReceivingTagBroadcasts();
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingTagBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingTagBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingTagBroadcasts();
		}
		this.enabled = enabled;
	}

	private void startReceivingTagBroadcasts() {
		if (!recieveTagBroadcasts) {
			LOGGER.debug("Start receiving tag broadcasts");

			recieveTagBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED);

			activity.registerReceiver(tagReceiver, filter, ANDROID_PERMISSION_NFC, null);
		}
	}

	private void stopReceivingTagBroadcasts() {
		if (recieveTagBroadcasts) {
			LOGGER.debug("Stop receiving tag broadcasts");

			recieveTagBroadcasts = false;

			activity.unregisterReceiver(tagReceiver);
		}
	}

}
