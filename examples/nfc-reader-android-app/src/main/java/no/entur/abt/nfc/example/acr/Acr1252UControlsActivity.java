package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1252UControlsActivity extends AbstractAcrControlsActivity<Acr1252UReader> {

	@Override
	protected Class<Acr1252UReader> getReaderType() {
		return Acr1252UReader.class;
	}

	@Override
	protected void addControls(Acr1252UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null, reader::getAutomaticPICCPolling,
				reader::setAutomaticPICCPolling);
		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.RED), null, reader::getLEDs, reader::setLEDs);
		addSection(getString(R.string.acr_controls_default_behaviour));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_GREEN, AcrDefaultLEDAndBuzzerBehaviour.COLOR_SELECT_LED_RED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);
		addSection(getString(R.string.acr_controls_buzzer));
		addSwitch(getString(R.string.acr_controls_buzzer), null, reader::setBuzzer);
	}
}
