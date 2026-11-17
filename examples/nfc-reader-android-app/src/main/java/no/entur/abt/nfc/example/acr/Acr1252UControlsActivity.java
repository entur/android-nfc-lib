package no.entur.abt.nfc.example.acr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

/**
 * ACR1252U, per the API manual 1.17.
 */

public class Acr1252UControlsActivity extends AbstractAcrControlsActivity<Acr1252UReader> {

	private static final String[] POLLING_BITS = { "Bit 0 auto PICC polling", "Bit 1 antenna off if no PICC found", "Bit 2 antenna off if PICC is inactive",
			"Bit 3 (RFU)", "Bit 4 poll interval (low)", "Bit 5 poll interval (high)", "Bit 6 NXP MIFARE PICC selection (fw 111.1+)", "Bit 7 enforce ISO 14443A part 4" };

	private static final String[] BEHAVIOUR_BITS = { "Bit 0 card operation blink LED", "Bit 1 PICC polling status LED", "Bit 2 PICC activation status LED",
			"Bit 3 card insertion buzzer", "Bit 4 card removal buzzer (needs bit 3)", "Bit 5 reader power on buzzer", "Bit 6 green LED for status",
			"Bit 7 red LED for status" };

	// automatic PPS speed codes
	private static final List<AcrCommunicationSpeed> SPEEDS = Arrays.asList(AcrCommunicationSpeed.RATE_106_KBPS, AcrCommunicationSpeed.RATE_212_KBPS,
			AcrCommunicationSpeed.RATE_424_KBPS);

	@Override
	protected Class<Acr1252UReader> getReaderType() {
		return Acr1252UReader.class;
	}

	@Override
	protected void addReaderDetails(Acr1252UReader reader) {
		addValue(getString(R.string.acr_controls_serial_number), () -> {
			// E1 00 00 00 Len serial number
			byte[] response = escape(reader, "E0 00 00 33 00");
			return new String(response, 5, response.length - 5, StandardCharsets.US_ASCII).trim();
		});
	}

	@Override
	protected void addControls(Acr1252UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ, AcrPICC.POLL_SRI_OR_SRIX), null, reader::getPICC, reader::setPICC);

		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null, reader::getAutomaticPICCPolling,
				reader::setAutomaticPICCPolling);

		addSection(getString(R.string.acr_controls_polling_bits));
		addBits(POLLING_BITS, () -> escape(reader, "E0 00 00 23 00")[5] & 0xFF, (value) -> (escape(reader, String.format(Locale.ROOT, "E0 00 00 23 01 %02X", value))[5] & 0xFF) == value);

		addAutomaticPPS(() -> {
			// E1 00 00 00 04 max Tx, current Tx, max Rx, current Rx
			byte[] response = escape(reader, "E0 00 00 24 00");
			List<AcrCommunicationSpeed> speeds = new ArrayList<>();
			for (int i = 5; i < 9; i++) {
				speeds.add(SPEEDS.get(response[i] & 0xFF));
			}
			return speeds;
		}, (tx, rx) -> {
			byte[] response = escape(reader, String.format(Locale.ROOT, "E0 00 00 24 02 %02X %02X", SPEEDS.indexOf(tx), SPEEDS.indexOf(rx)));
			return response.length >= 9 && response[5] == SPEEDS.indexOf(tx) && response[7] == SPEEDS.indexOf(rx);
		});

		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.RED), null, reader::getLEDs, reader::setLEDs);

		addSection(getString(R.string.acr_controls_default_behaviour));
		addNote(getString(R.string.acr_controls_1252_behaviour_note));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.READER_POWER_ON_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);

		addDefaultBehaviourBits(reader, BEHAVIOUR_BITS);

		addSection(getString(R.string.acr_controls_buzzer));
		addSwitch(getString(R.string.acr_controls_buzzer), null, reader::setBuzzer);
	}

	@Override
	protected List<String> getEscapeCommandPresets() {
		return Arrays.asList(
				"Read PICC operating parameter: E0 00 00 20 00",
				// other bit layout
				"Read PICC operating parameter (ACR122U compatible): FF 00 50 00 00",
				// fw 110.0+, Picopass in the second byte
				"Read PICC operating parameter (extended): E0 01 00 20 00",
				// fw 110.0+
				"Read PICC type: E0 00 00 35 00", "Read LED status: E0 00 00 29 00", "All LEDs off: E0 00 00 29 01 00", "Beep 100 ms: E0 00 00 28 01 0A");
	}
}
