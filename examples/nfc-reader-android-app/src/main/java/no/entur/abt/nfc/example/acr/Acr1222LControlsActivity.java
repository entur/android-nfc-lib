package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

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
