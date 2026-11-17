package no.entur.abt.nfc.example.acr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr122UReader;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

/**
 * ACR122U, per the API manual 2.04. The LEDs, timeout and antenna are not in the library API, so they use escape commands.
 */

public class Acr122UControlsActivity extends AbstractAcrControlsActivity<Acr122UReader> {

	// bi-color LED and buzzer control (FF 00 40), LED state control: final state bit 0 red, bit 1 green; state mask bit 2 red, bit 3 green
	private static final int LED_RED = 1;
	private static final int LED_GREEN = 1 << 1;
	private static final int LED_STATE_MASK = (1 << 2) | (1 << 3);

	@Override
	protected Class<Acr122UReader> getReaderType() {
		return Acr122UReader.class;
	}

	@Override
	protected void addControls(Acr122UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.AUTO_PICC_POLLING, AcrPICC.AUTO_ATS_GENERATION, AcrPICC.POLLING_INTERVAL, AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);

		addSection(getString(R.string.acr_controls_leds));
		addNote(getString(R.string.acr_controls_122_led_note));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.RED), null, () -> {
			// no state change, no blinking: only returns the current LED state
			return parseLEDs(ledControl(reader, "FF 00 40 00 04 00 00 00 00"));
		}, (leds) -> {
			int state = LED_STATE_MASK;
			for (AcrLED led : leds) {
				state |= led == AcrLED.RED ? LED_RED : LED_GREEN;
			}
			ledControl(reader, String.format(Locale.ROOT, "FF 00 40 %02X 04 00 00 00 00", state));
			return true;
		});

		addSection(getString(R.string.acr_controls_buzzer));
		addSwitch(getString(R.string.acr_controls_card_detection_beep), null, reader::setBuzzerForCardDetection);

		addSection(getString(R.string.acr_controls_timeout));
		addChoice(getString(R.string.acr_controls_timeout_parameter), Arrays.asList(0x00, 0x01, 0x02, 0x06, 0xFF), (value) -> {
			if (value == 0x00) {
				return getString(R.string.acr_controls_timeout_none);
			} else if (value == 0xFF) {
				return getString(R.string.acr_controls_timeout_wait);
			}
			return (value * 5) + " s";
		}, null, (value) -> isSuccess(escape(reader, String.format(Locale.ROOT, "FF 00 41 %02X 00", value))));

		addSection(getString(R.string.acr_controls_antenna));
		// not readable: the Field byte of the PN532 GetGeneralStatus is an external RF field, not the reader's own
		addSwitch(getString(R.string.acr_controls_antenna_field), null, (on) -> isSuccess(escape(reader, on ? "FF 00 00 00 04 D4 32 01 01" : "FF 00 00 00 04 D4 32 01 00")));
	}

	/**
	 * @return response 90 [current LED state]
	 */

	private byte[] ledControl(Acr122UReader reader, String command) {
		byte[] response = escape(reader, command);
		if (response.length != 2 || (response[0] & 0xFF) != 0x90) {
			throw new IllegalStateException("LED control failed");
		}
		return response;
	}

	private static List<AcrLED> parseLEDs(byte[] response) {
		List<AcrLED> leds = new ArrayList<>();
		if ((response[1] & LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}
		if ((response[1] & LED_RED) != 0) {
			leds.add(AcrLED.RED);
		}
		return leds;
	}

	private static boolean isSuccess(byte[] response) {
		return response.length >= 2 && (response[response.length - 2] & 0xFF) == 0x90 && response[response.length - 1] == 0x00;
	}

	@Override
	protected List<String> getEscapeCommandPresets() {
		return Arrays.asList(
				// read the PICC operating parameter
				"FF 00 50 00 00",
				// beep once for 100 ms (no LED change, buzzer during T1)
				"FF 00 40 00 04 01 01 01 01",
				// blink red / green 3 times with the buzzer, then green
				"FF 00 40 DE 04 02 02 03 01",
				// read the LED state
				"FF 00 40 00 04 00 00 00 00",
				// PN532 GetGeneralStatus
				"FF 00 00 00 02 D4 04");
	}
}
