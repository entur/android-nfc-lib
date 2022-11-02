package no.entur.android.nfc.external.acs.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrPICC;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.acs.reader.AcrReaderException;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.service.ExternalNfcReaderStatusListener;

public class AcrReaderListener implements ExternalNfcReaderStatusListener<AcrReader> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcrReaderListener.class);

	private final Context context;
	private Intent lastIntent;

	public AcrReaderListener(Context context) {
		this.context = context;
	}

	@Override
	public void onReaderClosed(int readerStatus, String statusMessage) {
		Intent intent = new Intent();
		intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

		if (readerStatus != -1) {
			intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_STATUS_CODE, readerStatus);
		}

		if (statusMessage != null) {
			intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_STATUS_MESSAGE, statusMessage);
		}

		sendBroadcastForNfcPermission(intent);
		this.lastIntent = intent;
	}

	@Override
	public void onReaderOpen(AcrReader reader, int status) {

		try {
			String name = reader.getName();

			String firmware = "unkown";
			try {
				firmware = reader.getFirmware();
			} catch (Exception e) {
				LOGGER.debug("Problem reading firmware", e);
			}
			List<AcrPICC> picc = reader.getPICC();

			LOGGER.debug("Got reader " + name + " with firmware " + firmware + " and PICC setting " + picc);

			// set reader-specific settings
			// note that the ATR parsing / card identification might be effected by the enabled modes
			// for HCE devices, and the current implementation is only a subset of the possibilites.
			// see https://smartcard-atr.apdu.fr/ for some help

			if (reader instanceof Acr122UReader) {
				Acr122UReader acr122uReader = (Acr122UReader) reader;
				acr122uReader.setBuzzerForCardDetection(true);

				acr122uReader.setPICC(AcrPICC.AUTO_PICC_POLLING, AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.AUTO_ATS_GENERATION);
			} else if (reader instanceof Acr1222LReader) {
				Acr1222LReader acr1222lReader = (Acr1222LReader) reader;

				// display font example - note that also font type C
				acr1222lReader.lightDisplayBacklight(true);
				acr1222lReader.clearDisplay();
			} else if (reader instanceof Acr1283LReader) {
				Acr1283LReader acr1283LReader = (Acr1283LReader) reader;

				// display font example - note that also font type C
				acr1283LReader.lightDisplayBacklight(true);
				acr1283LReader.clearDisplay();
			} else if (reader instanceof Acr1281UReader) {
				Acr1281UReader acr1281UReader = (Acr1281UReader) reader;

				acr1281UReader.setPICC(AcrPICC.POLL_ISO14443_TYPE_A);
				acr1281UReader.setAutomaticPICCPolling(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED,
						AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4);
			} else if (reader instanceof Acr1252UReader) {

				/** DEMO: Works with the Motorola Android device */

				Acr1252UReader acr1252UReader = (Acr1252UReader) reader;

				acr1252UReader.setPICC(AcrPICC.POLL_ISO14443_TYPE_A);
				acr1252UReader.setAutomaticPICCPolling(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED,
						AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4);
			} else if (reader instanceof Acr1255UReader) {
				Acr1255UReader bluetoothReader = (Acr1255UReader) reader;

				LOGGER.debug("Battery level is " + bluetoothReader.getBatteryLevel() + "%");

				bluetoothReader.setPICC(AcrPICC.POLL_ISO14443_TYPE_A);

				bluetoothReader.setAutomaticPICCPolling(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4,
						AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000);
				bluetoothReader.setAutomaticPolling(true);

				// XXX this seems to put the reader in a sort of bricked state
				// acr1255UReader.setSleepModeOption(-1); // no sleep
			}
		} catch (AcrReaderException e) {
			LOGGER.debug("Problem accessing reader", e);
		}

		Intent intent = new Intent();

		intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);
		intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, reader);

		intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_STATUS_CODE, status);

		sendBroadcastForNfcPermission(intent);
		this.lastIntent = intent;
	}

	@Override
	public void onReaderStatusIntent(Intent requestIntent) {
		Intent lastIntent = this.lastIntent;
		if(lastIntent != null) {
			LOGGER.debug("Broadcast currant status intent");
			sendBroadcastForNfcPermission(lastIntent);
		} else {
			LOGGER.debug("No 	status intent");
		}
	}

	private void sendBroadcastForNfcPermission(Intent intent) {
		LOGGER.debug("Broadcast " + intent.getAction());

		// broadcast to apps with NFC permission only, to make sonarclod happy
		context.sendBroadcast(intent, "android.permission.NFC");
	}
}
