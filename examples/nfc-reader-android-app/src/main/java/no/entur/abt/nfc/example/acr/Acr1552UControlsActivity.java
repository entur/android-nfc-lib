package no.entur.abt.nfc.example.acr;

import android.text.InputType;
import android.widget.EditText;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;

public class Acr1552UControlsActivity extends AbstractAcrControlsActivity<Acr1552UReader> {

	@Override
	protected Class<Acr1552UReader> getReaderType() {
		return Acr1552UReader.class;
	}

	@Override
	protected void addControls(Acr1552UReader reader) {
		addDefaultBehaviourSection(Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), reader::getDefaultLEDAndBuzzerBehaviour, reader::setDefaultLEDAndBuzzerBehaviour);
		addLEDSection(getString(R.string.acr_controls_leds), reader::getLEDs, reader::setLEDs);
		addSection(getString(R.string.acr_controls_buzzer));
		EditText duration = editText("200", InputType.TYPE_CLASS_NUMBER);
		addRow(text(getString(R.string.acr_controls_duration), false), duration, button(getString(R.string.acr_controls_beep), (v) -> {
			int millis = parseInt(duration, 200);
			run("setBuzzerControlSingle(" + millis + ")", () -> reader.setBuzzerControlSingle(millis));
		}), button(getString(R.string.acr_controls_beep_3), (v) -> run("setBuzzerControlRepeat(100, 100, 3)", () -> reader.setBuzzerControlRepeat(100, 100, 3))));
	}
}
