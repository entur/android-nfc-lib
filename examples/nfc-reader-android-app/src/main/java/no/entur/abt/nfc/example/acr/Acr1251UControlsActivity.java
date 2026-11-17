package no.entur.abt.nfc.example.acr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1251UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

/**
 * ACR1251U, per the API manual 1.14. The library only has the PICC operating parameter for this reader, so the other settings use
 * escape commands.
 */

public class Acr1251UControlsActivity extends AbstractAcrControlsActivity<Acr1251UReader> {

	// LED control (E0 00 00 29)
	private static final int LED_RED = 1;
	private static final int LED_GREEN = 1 << 1;

	// the default LED and buzzer behaviour bits differ between firmware 205 and below, and 206 and above
	private static final String[] BEHAVIOUR_BITS_205 = { "Bit 0 SAM activation status LED", "Bit 1 PICC polling status LED", "Bit 2 PICC activation status LED",
			"Bit 3 card insertion / removal buzzer", "Bit 4 (RFU)", "Bit 5 (RFU)", "Bit 6 (RFU)", "Bit 7 card operation blink LED" };
	private static final String[] BEHAVIOUR_BITS_206 = { "Bit 0 card operation blink LED", "Bit 1 PICC polling status LED", "Bit 2 PICC activation status LED",
			"Bit 3 card insertion / removal buzzer", "Bit 4 (RFU)", "Bit 5 reader power on buzzer", "Bit 6 green LED for status", "Bit 7 red LED for status" };
	// unknown firmware
	private static final String[] BEHAVIOUR_BITS = { "Bit 0 card operation blink LED (fw 206+), SAM activation LED (fw 205-)", "Bit 1 PICC polling status LED",
			"Bit 2 PICC activation status LED", "Bit 3 card insertion / removal buzzer", "Bit 4 (RFU)", "Bit 5 reader power on buzzer (fw 206+)",
			"Bit 6 green LED for status (fw 206+)", "Bit 7 red LED for status (fw 206+), card operation blink LED (fw 205-)" };

	@Override
	protected Class<Acr1251UReader> getReaderType() {
		return Acr1251UReader.class;
	}

	@Override
	protected void addControls(Acr1251UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);

		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null,
				() -> AcrAutomaticPICCPolling.parse(escapeValue(reader, "E0 00 00 23 00")), (types) -> {
					int value = AcrAutomaticPICCPolling.serialize(types);
					return escapeValue(reader, String.format(Locale.ROOT, "E0 00 00 23 01 %02X", value)) == value;
				});

		addAutomaticPPS(reader);

		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.RED), null, () -> {
			int value = escapeValue(reader, "E0 00 00 29 00");
			List<AcrLED> leds = new ArrayList<>();
			if ((value & LED_GREEN) != 0) {
				leds.add(AcrLED.GREEN);
			}
			if ((value & LED_RED) != 0) {
				leds.add(AcrLED.RED);
			}
			return leds;
		}, (leds) -> {
			int value = 0;
			for (AcrLED led : leds) {
				value |= led == AcrLED.RED ? LED_RED : LED_GREEN;
			}
			return escapeValue(reader, String.format(Locale.ROOT, "E0 00 00 29 01 %02X", value)) == value;
		});

		Boolean fw206 = isFirmwareAtLeast(206);
		addDefaultBehaviourBits(reader, fw206 == null ? BEHAVIOUR_BITS : fw206 ? BEHAVIOUR_BITS_206 : BEHAVIOUR_BITS_205);
	}

	/**
	 * @return the data byte of an E1 00 00 00 01 [value] response
	 */

	private int escapeValue(Acr1251UReader reader, String command) {
		byte[] response = escape(reader, command);
		if (response.length != 6 || (response[0] & 0xFF) != 0xE1) {
			throw new IllegalStateException("Unexpected response");
		}
		return response[5] & 0xFF;
	}

	@Override
	protected List<String> getEscapeCommandPresets() {
		return Arrays.asList("Beep 100 ms: E0 00 00 28 01 0A", "Read firmware: E0 00 00 18 00", "Read PICC operating parameter: E0 00 00 20 00",
				// other bit layout
				"Read PICC operating parameter (ACR122U compatible): FF 00 50 00 00", "Read automatic PICC polling: E0 00 00 23 00",
				"Read LED status: E0 00 00 29 00", "Read default LED and buzzer behaviour: E0 00 00 21 00");
	}
}
