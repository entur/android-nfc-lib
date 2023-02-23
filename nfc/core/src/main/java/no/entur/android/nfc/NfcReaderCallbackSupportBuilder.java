package no.entur.android.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import no.entur.android.nfc.wrapper.ReaderCallback;

public class NfcReaderCallbackSupportBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(NfcReaderCallbackSupport.class);

	protected Activity activity;
	protected ReaderCallback callback;
	protected Integer presenceCheckDelay;
	protected Executor executor; // optional

	public NfcReaderCallbackSupportBuilder withExecutor(Executor executor) {
		this.executor = executor;
		return this;
	}

	public NfcReaderCallbackSupportBuilder withActivity(Activity activity) {
		this.activity = activity;
		return this;
	}

	public NfcReaderCallbackSupportBuilder withReaderCallbackDelegate(ReaderCallback callback) {
		this.callback = callback;
		return this;
	}

	public NfcReaderCallbackSupportBuilder withPresenceCheckDelay(Integer value) {
		this.presenceCheckDelay = value;
		return this;
	}

	public NfcReaderCallbackSupport build() {
		if (activity == null) {
			throw new IllegalArgumentException();
		}
		if (callback == null) {
			throw new IllegalArgumentException();
		}

		Bundle options = null;
		if (presenceCheckDelay != null) {
			LOGGER.debug("Add NFC reader presence check delay " + presenceCheckDelay);
			options = new Bundle();
			options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, presenceCheckDelay);
		}

		return new NfcReaderCallbackSupport(activity, callback, options, executor);
	}

}
