package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;

public class Acr1281UControlsActivity extends AbstractAcrControlsActivity<Acr1281UReader> {

	@Override
	protected Class<Acr1281UReader> getReaderType() {
		return Acr1281UReader.class;
	}

	@Override
	protected void addControls(Acr1281UReader reader) {
		addDefaultBehaviourSection(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), reader::getDefaultLEDAndBuzzerBehaviour, reader::setDefaultLEDAndBuzzerBehaviour);
		addLEDSection(getString(R.string.acr_controls_leds), reader::getLEDs, reader::setLEDs);
	}
}
