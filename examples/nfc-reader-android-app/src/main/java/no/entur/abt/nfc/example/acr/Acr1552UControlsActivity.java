package no.entur.abt.nfc.example.acr;

import android.text.InputType;
import android.widget.EditText;

import java.util.Arrays;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1552UControlsActivity extends AbstractAcrControlsActivity<Acr1552UReader> {

	@Override
	protected Class<Acr1552UReader> getReaderType() {
		return Acr1552UReader.class;
	}

	@Override
	protected void addControls(Acr1552UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_TOPAZ, AcrPICC.POLL_INNOVATRON, AcrPICC.POLL_SRI_OR_SRIX, AcrPICC.POLL_PICOPASS_ISO14443B, AcrPICC.POLL_PICOPASS_ISO15693, AcrPICC.POLL_ISO15693, AcrPICC.POLL_CTS), null, reader::getPICC, reader::setPICC);
		// on the ACR1552U, bit 1 enables the RF off interval (bits 4 - 5): how long the RF field is off between polls
		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.POLLING_ENABLE_EXTRA_MIFARE_TYPE_IDENTIFICATION, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_1552_rf_off_interval), POLLING_INTERVALS,
				(value) -> value == AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND ? getString(R.string.acr_controls_1552_rf_off_interval_enable) : AcrLabels.label(value),
				reader::getAutomaticPICCPolling, reader::setAutomaticPICCPolling);

		addSection(getString(R.string.acr_controls_rf));
		addNote(getString(R.string.acr_controls_1552_rf_power_firmware));
		addChoice(getString(R.string.acr_controls_rf_power), Arrays.asList(0, 20, 40, 60, 80, 100), (percent) -> percent == 0 ? getString(R.string.acr_controls_rf_power_auto) : percent + " %",
				reader::getRadioFrequencyPower, reader::setRadioFrequencyPower);
		addChoice(getString(R.string.acr_controls_communication_speed), Arrays.asList(AcrCommunicationSpeed.values()), null, reader::getAutomaticCommunicationSpeed,
				reader::setAutomaticCommunicationSpeed);

		addSection(getString(R.string.acr_controls_leds));
		addFlags(AcrLED.class, Arrays.asList(AcrLED.GREEN, AcrLED.BLUE), null, reader::getLEDs, reader::setLEDs);
		addSection(getString(R.string.acr_controls_default_behaviour));
		addNote(getString(R.string.acr_controls_1552_behaviour_firmware));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);
		addSection(getString(R.string.acr_controls_buzzer));
		EditText duration = editText("200", InputType.TYPE_CLASS_NUMBER);
		addRow(text(getString(R.string.acr_controls_duration), false), duration, button(getString(R.string.acr_controls_beep), (v) -> {
			int millis = parseInt(duration, 200);
			run("setBuzzerControlSingle(" + millis + ")", () -> reader.setBuzzerControlSingle(millis));
		}), button(getString(R.string.acr_controls_beep_3), (v) -> run("setBuzzerControlRepeat(100, 100, 3)", () -> reader.setBuzzerControlRepeat(100, 100, 3))));
	}
}
