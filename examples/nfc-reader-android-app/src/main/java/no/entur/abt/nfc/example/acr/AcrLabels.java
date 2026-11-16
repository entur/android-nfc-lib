package no.entur.abt.nfc.example.acr;

import java.util.HashMap;
import java.util.Map;

import no.entur.android.nfc.external.acs.reader.AcrAntennaFieldStatus;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrBluetoothTransmissionPower;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

/**
 * Readable labels for the ACS reader settings.
 */

public class AcrLabels {

	private static final Map<Enum<?>, String> LABELS = new HashMap<>();

	static {
		LABELS.put(AcrPICC.AUTO_PICC_POLLING, "Automatic PICC polling");
		LABELS.put(AcrPICC.AUTO_ATS_GENERATION, "Automatic ATS generation");
		LABELS.put(AcrPICC.POLLING_INTERVAL, "Polling interval 250 ms (off: 500 ms)");
		LABELS.put(AcrPICC.POLL_ISO14443_TYPE_A, "ISO 14443 type A");
		LABELS.put(AcrPICC.POLL_ISO14443_TYPE_B, "ISO 14443 type B");
		LABELS.put(AcrPICC.POLL_FELICA_212K, "FeliCa 212 kbps");
		LABELS.put(AcrPICC.POLL_FELICA_424K, "FeliCa 424 kbps");
		LABELS.put(AcrPICC.POLL_TOPAZ, "Topaz");
		LABELS.put(AcrPICC.POLL_INNOVATRON, "Innovatron");
		LABELS.put(AcrPICC.POLL_SRI_OR_SRIX, "SRI / SRIX");
		LABELS.put(AcrPICC.POLL_PICOPASS_ISO14443B, "Picopass ISO 14443B");
		LABELS.put(AcrPICC.POLL_PICOPASS_ISO15693, "Picopass ISO 15693");
		LABELS.put(AcrPICC.POLL_ISO15693, "ISO 15693");
		LABELS.put(AcrPICC.POLL_CTS, "CTS");

		LABELS.put(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, "Automatic PICC polling");
		LABELS.put(AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, "Turn off antenna field if no PICC found");
		LABELS.put(AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, "Turn off antenna field if PICC is inactive");
		LABELS.put(AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED, "Activate PICC when detected");
		LABELS.put(AcrAutomaticPICCPolling.POLLING_ENABLE_EXTRA_MIFARE_TYPE_IDENTIFICATION, "Extra MIFARE type identification");
		LABELS.put(AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4, "Enforce ISO 14443A part 4");
		LABELS.put(AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_250, "250 ms");
		LABELS.put(AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_500, "500 ms");
		LABELS.put(AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_1000, "1 s");
		LABELS.put(AcrAutomaticPICCPolling.PICC_POLLING_INTERVAL_2500, "2.5 s");

		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, "PICC polling status LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, "PICC activation status LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED, "ICC activation status LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.BATTERY_CHARGING_STATUS_LED, "Battery charging status LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, "Blink LED on card operation");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN, "Use green LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED, "Use red LED");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, "Beep on card insertion / removal");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER, "Beep on card insertion");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER, "Beep on card removal");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER, "Beep on contactless chip reset");
		LABELS.put(AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER, "Beep on exclusive mode change");

		LABELS.put(AcrLED.GREEN, "Green");
		LABELS.put(AcrLED.BLUE, "Blue");
		LABELS.put(AcrLED.ORANGE, "Orange");
		LABELS.put(AcrLED.RED, "Red");

		LABELS.put(AcrBluetoothTransmissionPower.DISTANCE_3M, "3 m");
		LABELS.put(AcrBluetoothTransmissionPower.DISTANCE_7M, "7 m");
		LABELS.put(AcrBluetoothTransmissionPower.DISTANCE_17M, "17 m");
		LABELS.put(AcrBluetoothTransmissionPower.DISTANCE_25M, "25 m");

		LABELS.put(AcrCommunicationSpeed.RATE_106_KBPS, "106");
		LABELS.put(AcrCommunicationSpeed.RATE_212_KBPS, "212");
		LABELS.put(AcrCommunicationSpeed.RATE_424_KBPS, "424");
		LABELS.put(AcrCommunicationSpeed.RATE_848_KBPS, "848");

		LABELS.put(AcrAntennaFieldStatus.PICC_POWER_OFF, "Off");
		LABELS.put(AcrAntennaFieldStatus.PICC_IDLE, "Idle");
		LABELS.put(AcrAntennaFieldStatus.PICC_READY, "Ready");
		LABELS.put(AcrAntennaFieldStatus.PICC_SELECTED, "Selected");
		LABELS.put(AcrAntennaFieldStatus.PICC_ACTIVE, "Active");
	}

	private AcrLabels() {
	}

	public static String label(Object value) {
		String label = LABELS.get(value);
		if (label != null) {
			return label;
		}
		return String.valueOf(value);
	}
}
