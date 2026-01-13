package no.entur.android.nfc.external;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import no.entur.android.nfc.util.RegisterReceiverUtils;

public class ExternalBarcodeCallbackSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalBarcodeCallbackSupport.class);

	public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

	protected final ExternalBarcodeCallback callback;
	protected final Context context;

	private boolean recieveBarcodeBroadcasts = false;

	protected boolean enabled = false;
	protected Executor executor; // non-final for testing

	protected boolean receiverExported;

	private final BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
            LOGGER.debug("Action " + intent.getAction());
		if (intent.getAction().equals(ExternalBarcodeCallback.ACTION_BARCODE_DISCOVERED)) {

			byte[] bytes = intent.getByteArrayExtra(ExternalBarcodeCallback.BARCODE_EXTRA_BODY);

			if(executor != null) {
				executor.execute(() -> {
					callback.onBarcodeDiscovered(bytes, intent);
				});
			} else {
				callback.onBarcodeDiscovered(bytes, intent);
			}

		} else {
			LOGGER.debug("Ignore action " + intent.getAction());
		}
		}
	};

	public ExternalBarcodeCallbackSupport(ExternalBarcodeCallback callback, Context context, Executor executor, boolean receiverExported) {
		this.callback = callback;
		this.context = context;
		this.executor = executor;
		this.receiverExported = receiverExported;
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
		if (!recieveBarcodeBroadcasts) {
			LOGGER.debug("Start receiving tag broadcasts");

			recieveBarcodeBroadcasts = true;

			// register receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(ExternalBarcodeCallback.ACTION_BARCODE_DISCOVERED);
			RegisterReceiverUtils.registerReceiver(
					context,
                    barcodeReceiver,
					filter,
					ANDROID_PERMISSION_NFC,
					null,
					receiverExported

			);
		}
	}

	private void stopReceivingTagBroadcasts() {
		if (recieveBarcodeBroadcasts) {
			LOGGER.debug("Stop receiving tag broadcasts");

			recieveBarcodeBroadcasts = false;

			context.unregisterReceiver(barcodeReceiver);
		}
	}

}
