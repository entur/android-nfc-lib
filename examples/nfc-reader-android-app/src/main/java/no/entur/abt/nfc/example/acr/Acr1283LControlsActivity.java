package no.entur.abt.nfc.example.acr;

import android.text.InputType;
import android.widget.EditText;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1283LControlsActivity extends AbstractAcrControlsActivity<Acr1283LReader> {

	@Override
	protected Class<Acr1283LReader> getReaderType() {
		return Acr1283LReader.class;
	}

	@Override
	protected void addControls(Acr1283LReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null, reader::getAutomaticPICCPolling,
				reader::setAutomaticPICCPolling);
		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.BLUE, AcrLED.ORANGE, AcrLED.RED), this::statusLabel, null, reader::setLEDs);
		addSection(getString(R.string.acr_controls_default_behaviour));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);
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
		}, reader::setDisplayContrast);
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
