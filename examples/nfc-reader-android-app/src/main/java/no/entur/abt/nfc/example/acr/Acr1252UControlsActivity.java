package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;

public class Acr1252UControlsActivity extends AbstractAcrControlsActivity<Acr1252UReader> {

	@Override
	protected Class<Acr1252UReader> getReaderType() {
		return Acr1252UReader.class;
	}

	@Override
	protected void addControls(Acr1252UReader reader) {
		addDefaultBehaviourSection(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED), reader::getDefaultLEDAndBuzzerBehaviour, reader::setDefaultLEDAndBuzzerBehaviour);
		addLEDSection(getString(R.string.acr_controls_leds), reader::getLEDs, reader::setLEDs);
		addSection(getString(R.string.acr_controls_buzzer));
		addRow(button(getString(R.string.acr_controls_on), (v) -> run("setBuzzer(true)", () -> reader.setBuzzer(true))),
				button(getString(R.string.acr_controls_off), (v) -> run("setBuzzer(false)", () -> reader.setBuzzer(false))));
	}
}
