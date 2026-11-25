package no.entur.abt.nfc.example.acr;

import android.text.InputType;
import android.widget.EditText;

import java.util.Arrays;
import java.util.List;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1222LReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1222LControlsActivity extends AbstractAcrControlsActivity<Acr1222LReader> {

	@Override
	protected Class<Acr1222LReader> getReaderType() {
		return Acr1222LReader.class;
	}

	@Override
	protected void addControls(Acr1222LReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.AUTO_PICC_POLLING, AcrPICC.AUTO_ATS_GENERATION, AcrPICC.POLLING_INTERVAL, AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.BLUE, AcrLED.ORANGE, AcrLED.RED), this::statusLabel, null, reader::setLEDs);
		addSection(getString(R.string.acr_controls_default_behaviour));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);
		addDefaultBehaviourBits(reader, BEHAVIOUR_BITS);

		addSection(getString(R.string.acr_controls_buzzer));
		EditText duration = editText("100", InputType.TYPE_CLASS_NUMBER);
		addRow(text(getString(R.string.acr_controls_duration), false), duration, button(getString(R.string.acr_controls_beep), (v) -> {
			int millis = parseInt(duration, 100);
			run("setBuzzerControlSingle(" + millis + ")", () -> reader.setBuzzerControlSingle(millis));
		}));

		addSection(getString(R.string.acr_controls_display));
		addDisplay(new Display() {
			@Override
			public boolean displayText(AcrFont font, int style, int line, String text) {
				return reader.displayText(font, style, line, 0, text);
			}

			@Override
			public boolean clear() {
				return reader.clearDisplay();
			}

			@Override
			public boolean backlight(boolean on) {
				return reader.lightDisplayBacklight(on);
			}
		}, null);
	}

	private static final String[] BEHAVIOUR_BITS = { "Bit 0 (RFU)", "Bit 1 PICC polling status LED", "Bit 2 PICC activation status LED", "Bit 3 (RFU)",
			"Bit 4 card insertion / removal buzzer", "Bit 5 (RFU)", "Bit 6 (RFU)", "Bit 7 card operation blink LED" };

	@Override
	protected List<String> getEscapeCommandPresets() {
		return Arrays.asList(
				"Read default LED and buzzer behaviour: E0 00 00 21 00", "Behaviour factory default: E0 00 00 21 01 8F",
				"Behaviour without bit 0: E0 00 00 21 01 8E", "Behaviour without bit 3: E0 00 00 21 01 87",
				"Behaviour with bit 4, not bit 3: E0 00 00 21 01 97", "Beep 100 ms: E0 00 00 28 01 0A", "All LEDs on: FF 00 44 0F 00",
				"All LEDs off: FF 00 44 00 00");
	}

	private String statusLabel(AcrLED led) {
		switch (led) {
		case GREEN:
			return getString(R.string.acr_controls_led_ready);
		case BLUE:
			return getString(R.string.acr_controls_led_progress);
		case ORANGE:
			return getString(R.string.acr_controls_led_complete);
		default:
			return getString(R.string.acr_controls_led_error);
		}
	}
}
