package no.entur.android.nfc.external;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import no.entur.android.nfc.util.RegisterReceiverUtils;

public class ExternalNfcIntentCallbackSupport {

	private static final List<String> DEFAULT_ACTIONS = Arrays.asList(ExternalNfcTagCallback.ACTION_TAG_DISCOVERED, ExternalNfcTagCallback.ACTION_TECH_DISCOVERED, ExternalNfcTagCallback.ACTION_NDEF_DISCOVERED);

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalNfcIntentCallbackSupport.class);

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalNfcIntentCallback callback;
	protected final Context context;
	protected final List<String> actions;
	protected Executor executor; // non-final for testing

	private boolean recieveNfcIntentBroadcasts = false;

	protected boolean enabled = false;

	private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if(executor != null) {
				executor.execute(() -> {
					callback.onNfcIntent(intent);
				});
			} else {
				callback.onNfcIntent(intent);
			}
		}
	};

	public ExternalNfcIntentCallbackSupport(ExternalNfcIntentCallback callback, Context context, Executor executor) {
		this(DEFAULT_ACTIONS, callback, context, executor);
	}

	public ExternalNfcIntentCallbackSupport(List<String> actions, ExternalNfcIntentCallback callback, Context context, Executor executor) {
		this.callback = callback;
		this.context = context;
		this.actions = actions;
		this.executor = executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public void onResume() {
		if (enabled) {
			startReceivingNfcIntentBroadcasts();
		}
	}

	public void onPause() {
		if (enabled) {
			stopReceivingNfcIntentBroadcasts();
		}
	}

	public void setEnabled(boolean enabled) {
		if (!this.enabled && enabled) {
			// disabled -> enabled
			startReceivingNfcIntentBroadcasts();
		} else if (this.enabled && !enabled) {

			// enabled -> disabled
			stopReceivingNfcIntentBroadcasts();
		}
		this.enabled = enabled;
	}

	protected void broadcast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
	}

	private void startReceivingNfcIntentBroadcasts() {
		if (!recieveNfcIntentBroadcasts) {
			LOGGER.debug("Start receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			for(String action : actions) {
				filter.addAction(action);
			}
			RegisterReceiverUtils.registerReceiver(
					context,
					intentReceiver,
					filter,
					ANDROID_PERMISSION_NFC,
					null,
					ContextCompat.RECEIVER_NOT_EXPORTED
			);
		}
	}

	private void stopReceivingNfcIntentBroadcasts() {
		if (recieveNfcIntentBroadcasts) {
			LOGGER.debug("Stop receiving nfc intent broadcasts");

			recieveNfcIntentBroadcasts = false;

			context.unregisterReceiver(intentReceiver);
		}
	}

}
