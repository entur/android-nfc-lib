package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1283LReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrFont;

public class Acr1283LControlsActivity extends AbstractAcrControlsActivity<Acr1283LReader> {

	@Override
	protected Class<Acr1283LReader> getReaderType() {
		return Acr1283LReader.class;
	}

	@Override
	protected void addControls(Acr1283LReader reader) {
		addDefaultBehaviourSection(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), reader::getDefaultLEDAndBuzzerBehaviour, reader::setDefaultLEDAndBuzzerBehaviour);
		addLEDSection(getString(R.string.acr_controls_leds), null, reader::setLEDs);
		addStatusLEDSection((values) -> reader.lightLED(values[0], values[1], values[2], values[3]));
		addDisplaySection(new Display() {
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
}
