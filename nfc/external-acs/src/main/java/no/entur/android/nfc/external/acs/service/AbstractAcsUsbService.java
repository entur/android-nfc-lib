package no.entur.android.nfc.external.acs.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.acs.smartcard.RemovedCardException;

import org.nfctools.api.TagType;
import org.nfctools.spi.acs.AcsTag;

import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.service.ExternalUsbNfcServiceSupport;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public abstract class AbstractAcsUsbService extends AbstractService {

	static final String[] STATE_STRINGS = { "Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

	private static final String TAG = AbstractAcsUsbService.class.getName();

	protected AcrExternalUsbNfcServiceSupport acrExternalUsbNfcServiceSupport;
	protected AcrReaderListener acrReaderListener = new AcrReaderListener(this);
	protected ExternalUsbNfcServiceSupport support;
	protected ReaderWrapper reader;

	@Override
	public void onCreate() {
		super.onCreate();

		// Get USB manager
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Initialize reader
		reader = new ReaderWrapper(manager);

		acrExternalUsbNfcServiceSupport = new AcrExternalUsbNfcServiceSupport(reader, binder);
		acrReaderListener = new AcrReaderListener(this);
		support = new ExternalUsbNfcServiceSupport(this, acrReaderListener, acrExternalUsbNfcServiceSupport);

		support.onCreate();

		reader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

			@Override
			public void onStateChange(int slot, int prevState, int currState) {

				// Log.d(TAG, "From state " + prevState + " to " + currState);

				if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
					prevState = Reader.CARD_UNKNOWN;
				}

				if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
					currState = Reader.CARD_UNKNOWN;
				}

				if (prevState == Reader.CARD_ABSENT && currState == Reader.CARD_PRESENT) {
					// Log.v(TAG, "Tag present on reader");

					onTagPresent(slot);
				} else if (currState == Reader.CARD_ABSENT) {
					// Log.v(TAG, "Tag absent on reader");

					onTagAbsent(slot);
				} else {
					Log.d(TAG, "Not action for state transition from " + STATE_STRINGS[prevState] + " to " + STATE_STRINGS[currState]);
				}

			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		broadcast(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);

		support.scanForDevices();

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		broadcast(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

		support.onDestroy();
	}

	public void onTagPresent(int slot) {
		new InitTagTask().execute(slot);
	}

	public void onTagAbsent(int slot) {
		Log.i(TAG, "onTagAbsent");

		store.removeItem(slot);

		Intent intent = new Intent();
		intent.setAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);
		sendBroadcast(intent, "android.permission.NFC");
	}

	private class InitTagTask extends AsyncTask<Integer, Void, Exception> {

		@Override
		protected Exception doInBackground(Integer... params) {

			Exception result = null;

			int slotNumber = params[0];

			try {
				Log.i(TAG, "Init tag at slot " + slotNumber);

				// https://en.wikipedia.org/wiki/Answer_to_reset#General_structure
				// http://smartcard-atr.appspot.com

				byte[] atr = reader.power(slotNumber, Reader.CARD_WARM_RESET);
				if (atr == null) {
					Log.d(TAG, "No ATR, ignoring");

					return null;
				}
				final TagType tagType = TagUtility.identifyTagType(reader.getReaderName(), atr);

				Log.d(TAG, "" + tagType + " for ATR " + ByteArrayHexStringConverter.toHexString(atr));

				handleTagInit(slotNumber, atr, tagType);
			} catch (RemovedCardException e) {
				Log.d(TAG, "Tag removed before it could be powered; ignore.", e);
			} catch (Exception e) {
				Log.w(TAG, "Problem initiating tag", e);

				TagUtility.sendTechBroadcast(AbstractAcsUsbService.this);

				result = e;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Exception result) {
			// publish result?
		}
	}

	public void handleTagInit(int slotNumber, byte[] atr, TagType tagType) throws ReaderException {
		int preferredProtocols = Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1;
		reader.setProtocol(0, preferredProtocols);

		int state = reader.getState(slotNumber);
		if (state != Reader.CARD_SPECIFIC) {
			TagUtility.sendTechBroadcast(this);
		} else {
			handleTagInitRegularMode(slotNumber, atr, tagType);
		}
	}

	protected abstract void handleTagInitRegularMode(int slotNumber, byte[] atr, TagType tagType);


}
