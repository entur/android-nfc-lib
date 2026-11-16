package no.entur.abt.nfc.example.acr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;

public class Acr1255UControlsActivity extends AbstractAcrControlsActivity<Acr1255UReader> {

	@Override
	protected Class<Acr1255UReader> getReaderType() {
		return Acr1255UReader.class;
	}

	@Override
	protected void addControls(Acr1255UReader reader) {
		addDefaultBehaviourSection(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.ICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_AND_REMOVAL_EVENTS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CONTACTLESS_CHIP_RESET_INDICATION_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.EXCLUSIVE_MODE_STATUS_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), reader::getDefaultLEDAndBuzzerBehaviour, reader::setDefaultLEDAndBuzzerBehaviour);
		// two LEDs, set to the same value here
		addLEDSection(getString(R.string.acr_controls_leds), () -> {
			List<Set<AcrLED>> leds = reader.getLEDs();
			return leds.isEmpty() ? new ArrayList<>() : new ArrayList<>(leds.get(0));
		}, (leds) -> {
			Set<AcrLED> set = EnumSet.noneOf(AcrLED.class);
			set.addAll(Arrays.asList(leds));
			return reader.setLEDs(set, set);
		});
	}
}
