package no.entur.abt.nfc.example.acr;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1281UControlsActivity extends AbstractAcrControlsActivity<Acr1281UReader> {

	@Override
	protected Class<Acr1281UReader> getReaderType() {
		return Acr1281UReader.class;
	}

	@Override
	protected void addControls(Acr1281UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null, reader::getAutomaticPICCPolling,
				reader::setAutomaticPICCPolling);
		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.RED), null, reader::getLEDs, reader::setLEDs);
		addSection(getString(R.string.acr_controls_default_behaviour));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);
		addSection(getString(R.string.acr_controls_mode));
		addSwitch(getString(R.string.acr_controls_exclusive_mode), reader::getExclusiveMode, (exclusive) -> reader.setExclusiveMode(!exclusive));
	}
}
