package no.entur.android.nfc.external.acs.service;

import android.app.Service;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.acs.smartcard.RemovedCardException;

import org.nfctools.api.detect.DefaultTagTypeDetector;
import org.nfctools.api.TagType;
import org.nfctools.api.detect.TagTypeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.tag.TagUtility;
import no.entur.android.nfc.external.service.AbstractService;
import no.entur.android.nfc.external.service.ExternalNfcReaderStatusListener;
import no.entur.android.nfc.external.service.ExternalNfcReaderStatusSupport;
import no.entur.android.nfc.external.service.ExternalUsbNfcServiceSupport;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public abstract class AbstractAcsUsbService extends AbstractService implements ExternalNfcReaderStatusListener<WrappedAcrReader> {

	static final String[] STATE_STRINGS = { "Unknown", "Absent", "Present", "Swallowed", "Powered", "Negotiable", "Specific" };

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAcsUsbService.class);

	protected AcrReaderAdapter acrReaderAdapter;
	protected AcrReaderListener acrReaderListener = new AcrReaderListener(this);

	protected ExternalNfcReaderStatusSupport externalNfcReaderStatusSupport = new ExternalNfcReaderStatusSupport(this, acrReaderListener, false);

	protected TagTypeDetector<ReaderWrapper> tagTypeDetector = new DefaultTagTypeDetector<>();
	protected ExternalUsbNfcServiceSupport support;

	protected boolean receiverExported = false;

	public void onReaderClosed(int readerStatus, String statusMessage) {
		acrReaderListener.onReaderClosed(readerStatus, statusMessage);
	}

	public void onReaderOpen(WrappedAcrReader reader, int readerStatusOk) {
		acrReaderListener.onReaderOpen(reader, readerStatusOk);

		initialize(reader.getReaderWrapper());
	}

	public void onReaderStatusIntent(Intent intent) {
		acrReaderListener.onReaderStatusIntent(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		acrReaderAdapter = new AcrReaderAdapter(this, binder);

		support = new ExternalUsbNfcServiceSupport(this, this, acrReaderAdapter, receiverExported);
		support.onCreate();

		externalNfcReaderStatusSupport.onResume();
	}

	private void initialize(ReaderWrapper reader) {


		reader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

			@Override
			public void onStateChange(int slot, int prevState, int currState) {

				// LOGGER.debug("From state " + prevState + " to " + currState);

				if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
					prevState = Reader.CARD_UNKNOWN;
				}

				if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
					currState = Reader.CARD_UNKNOWN;
				}

				if (prevState == Reader.CARD_ABSENT && currState == Reader.CARD_PRESENT) {
					// Log.v(TAG, "Tag present on reader");

					onTagPresent(reader, slot);
				} else if (currState == Reader.CARD_ABSENT) {
					// Log.v(TAG, "Tag absent on reader");

					onTagAbsent(slot);
				} else {
					LOGGER.debug("Not action for state transition from " + STATE_STRINGS[prevState] + " to " + STATE_STRINGS[currState]);
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

		externalNfcReaderStatusSupport.onPause();

		if(support != null) {
			support.onDestroy();
		}

		acrReaderAdapter.close();
	}

	public void onTagPresent(ReaderWrapper reader, int slot) {
		new InitTagTask(reader).execute(slot);
	}

	public void onTagAbsent(int slot) {
		LOGGER.info("onTagAbsent");

        Intent intent = new Intent();
        intent.setAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);

        TagProxy proxy = store.removeItem(slot);
        if(proxy != null) {
            byte[] uid = proxy.getUid();
            if(uid != null) {
                intent.putExtra(NfcAdapter.EXTRA_ID, uid);
            }
            intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, proxy.getHandle());
            intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "mock");
        }

		sendBroadcast(intent, "android.permission.NFC");
	}

	private class InitTagTask extends AsyncTask<Integer, Void, Exception> {

		private ReaderWrapper reader;

		private InitTagTask(ReaderWrapper reader) {
			this.reader = reader;
		}

		@Override
		protected Exception doInBackground(Integer... params) {

			Exception result = null;

			int slotNumber = params[0];

			try {
				LOGGER.info("Init tag at slot " + slotNumber);

				// Remove previous card on the same slot, if any.
				// Normally this is done by a tag lost even, but added here
				// as a precaution
				store.removeItem(slotNumber);

				// https://en.wikipedia.org/wiki/Answer_to_reset#General_structure
				// http://smartcard-atr.appspot.com

				byte[] atr = reader.power(slotNumber, Reader.CARD_WARM_RESET);
				if (atr == null) {
					LOGGER.debug("No ATR, ignoring");

					return null;
				}
				final TagType tagType = tagTypeDetector.parseAtr(reader, atr);

				LOGGER.debug("Tag inited as " + tagType + " for ATR " + ByteArrayHexStringConverter.toHexString(atr));

				handleTagInit(reader, slotNumber, atr, tagType);
			} catch (RemovedCardException e) {
				LOGGER.debug("Tag removed before it could be powered; ignore.", e);
			} catch (Exception e) {
				LOGGER.warn("Problem initiating tag", e);

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

	public void handleTagInit(ReaderWrapper reader, int slotNumber, byte[] atr, TagType tagType) throws ReaderException {
		int preferredProtocols = Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1;
		reader.setProtocol(0, preferredProtocols);

		int state = reader.getState(slotNumber);
		if (state != Reader.CARD_SPECIFIC) {
			TagUtility.sendTechBroadcast(this);
		} else {
			handleTagInitRegularMode(reader, slotNumber, atr, tagType);
		}
	}


	protected abstract void handleTagInitRegularMode(ReaderWrapper reader, int slotNumber, byte[] atr, TagType tagType);


}
