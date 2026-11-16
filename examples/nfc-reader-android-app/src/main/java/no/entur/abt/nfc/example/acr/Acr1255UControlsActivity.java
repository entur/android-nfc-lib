package no.entur.abt.nfc.example.acr;

import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import no.entur.abt.nfc.example.R;
import no.entur.android.nfc.external.acs.reader.Acr1255UReader;
import no.entur.android.nfc.external.acs.reader.AcrAntennaFieldStatus;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrBluetoothTransmissionPower;
import no.entur.android.nfc.external.acs.reader.AcrCommunicationSpeed;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrPICC;

public class Acr1255UControlsActivity extends AbstractAcrControlsActivity<Acr1255UReader> {

	@Override
	protected Class<Acr1255UReader> getReaderType() {
		return Acr1255UReader.class;
	}

	@Override
	protected void addReaderDetails(Acr1255UReader reader) {
		addValue(getString(R.string.acr_controls_serial_number), reader::getSerialNumber);
		addValue(getString(R.string.acr_controls_battery), reader::getBatteryLevel);
		addValue(getString(R.string.acr_controls_antenna_field_status), reader::getAntennaFieldStatus);
	}

	@Override
	protected void addControls(Acr1255UReader reader) {
		addSection(getString(R.string.acr_controls_picc));
		addFlags(AcrPICC.class, Arrays.asList(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B, AcrPICC.POLL_FELICA_212K, AcrPICC.POLL_FELICA_424K, AcrPICC.POLL_TOPAZ), null, reader::getPICC, reader::setPICC);
		addSection(getString(R.string.acr_controls_polling));
		addFlags(AcrAutomaticPICCPolling.class, Arrays.asList(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_NO_PICC_FOUND, AcrAutomaticPICCPolling.TURN_ANTENNA_FIELD_IF_PICC_IS_INACTIVE, AcrAutomaticPICCPolling.POLLING_ENABLE_EXTRA_MIFARE_TYPE_IDENTIFICATION, AcrAutomaticPICCPolling.ENFORCE_ISO14443A_PART_4), getString(R.string.acr_controls_polling_interval), POLLING_INTERVALS, null, reader::getAutomaticPICCPolling,
				reader::setAutomaticPICCPolling);
		addSwitch(getString(R.string.acr_controls_automatic_polling), null, reader::setAutomaticPolling);

		addAutomaticPPS(reader);
		addLEDs(reader);

		addSection(getString(R.string.acr_controls_default_behaviour));
		addNote(getString(R.string.acr_controls_1255_behaviour_note));
		addFlags(AcrDefaultLEDAndBuzzerBehaviour.class, Arrays.asList(AcrDefaultLEDAndBuzzerBehaviour.BATTERY_CHARGING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_POLLING_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.PICC_ACTIVATION_STATUS_LED, AcrDefaultLEDAndBuzzerBehaviour.CARD_INSERTION_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_REMOVAL_EVENT_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.READER_POWER_ON_BUZZER, AcrDefaultLEDAndBuzzerBehaviour.CARD_OPERATION_BLINK_LED), null, reader::getDefaultLEDAndBuzzerBehaviour,
				reader::setDefaultLEDAndBuzzerBehaviour);

		addDefaultBehaviourBits(reader, BEHAVIOUR_BITS);

		addSection(getString(R.string.acr_controls_antenna));
		addSwitch(getString(R.string.acr_controls_antenna_field), () -> reader.getAntennaFieldStatus() != AcrAntennaFieldStatus.PICC_POWER_OFF, reader::setAntennaField);

		addSection(getString(R.string.acr_controls_bluetooth));
		addChoice(getString(R.string.acr_controls_bluetooth_power), Arrays.asList(AcrBluetoothTransmissionPower.values()), null, reader::getBluetoothTransmissionPower,
				reader::setBluetoothTransmissionPower);
		addChoice(getString(R.string.acr_controls_sleep), Arrays.asList(60, 90, 120, 180, -1),
				(seconds) -> seconds == -1 ? getString(R.string.acr_controls_sleep_never) : seconds + " s", null, reader::setSleepModeOption);
	}

	// per the ACR1255U-J1 reference manual 1.13
	private static final String[] BEHAVIOUR_BITS = { "Bit 0 battery charging status LED", "Bit 1 PICC polling status LED", "Bit 2 PICC activation status LED",
			"Bit 3 card insertion buzzer", "Bit 4 card removal buzzer", "Bit 5 reader power on buzzer", "Bit 6 (RFU)", "Bit 7 card operation blink LED" };

	@Override
	protected List<String> getEscapeCommandPresets() {
		return Arrays.asList(
				// automatic polling on / off
				"E0 00 00 40 01", "E0 00 00 40 00",
				// read the LED status, all LEDs off
				"E0 00 00 29 00", "E0 00 00 29 01 00",
				// read the default LED and buzzer behaviour
				"E0 00 00 21 00",
				// buzzer on for 100 ms
				"E0 00 00 28 01 0A");
	}

	private void addAutomaticPPS(Acr1255UReader reader) {
		addSection(getString(R.string.acr_controls_pps));
		TextView current = addValueRow(getString(R.string.acr_controls_pps_current), "…");

		List<AcrCommunicationSpeed> speeds = Arrays.asList(AcrCommunicationSpeed.RATE_106_KBPS, AcrCommunicationSpeed.RATE_212_KBPS, AcrCommunicationSpeed.RATE_424_KBPS);
		RadioGroup[] groups = new RadioGroup[2];
		Runnable[] reload = new Runnable[1];
		Runnable apply = () -> {
			AcrCommunicationSpeed tx = getSelected(groups[0]);
			AcrCommunicationSpeed rx = getSelected(groups[1]);
			if (tx != null && rx != null) {
				run(getString(R.string.acr_controls_pps) + " = " + tx + ", " + rx, () -> reader.setAutomaticPICCPollingSpeed(tx, rx));
				reload[0].run();
			}
		};
		addSubheading(getString(R.string.acr_controls_pps_tx));
		groups[0] = addRadioGroup(speeds, AcrLabels::label, apply);
		addSubheading(getString(R.string.acr_controls_pps_rx));
		groups[1] = addRadioGroup(speeds, AcrLabels::label, apply);

		List<View> controls = getChildren(groups[0]);
		controls.addAll(getChildren(groups[1]));

		// max tx, current tx, max rx, current rx
		reload[0] = load("read " + getString(R.string.acr_controls_pps), reader::getAutomaticPICCPollingSpeed, (speed) -> {
			if (speed.size() >= 4) {
				setSelected(groups[0], speed.get(0));
				setSelected(groups[1], speed.get(2));
				current.setText(getString(R.string.acr_controls_pps_value, AcrLabels.label(speed.get(1)), AcrLabels.label(speed.get(3))));
			}
		}, controls);
	}

	/**
	 * Two LEDs: green and red, and blue and red.
	 */

	private void addLEDs(Acr1255UReader reader) {
		addSection(getString(R.string.acr_controls_leds));

		SwitchCompat[] switches = new SwitchCompat[4];
		Runnable[] reload = new Runnable[1];
		Runnable apply = () -> {
			Set<AcrLED> first = EnumSet.noneOf(AcrLED.class);
			if (switches[0].isChecked()) {
				first.add(AcrLED.GREEN);
			}
			if (switches[1].isChecked()) {
				first.add(AcrLED.RED);
			}
			Set<AcrLED> second = EnumSet.noneOf(AcrLED.class);
			if (switches[2].isChecked()) {
				second.add(AcrLED.BLUE);
			}
			if (switches[3].isChecked()) {
				second.add(AcrLED.RED);
			}
			run(getString(R.string.acr_controls_leds) + " = " + first + ", " + second, () -> reader.setLEDs(first, second));
			reload[0].run();
		};
		addSubheading(getString(R.string.acr_controls_led_1));
		switches[0] = addSwitchRow(AcrLabels.label(AcrLED.GREEN), apply);
		switches[1] = addSwitchRow(AcrLabels.label(AcrLED.RED), apply);
		addSubheading(getString(R.string.acr_controls_led_2));
		switches[2] = addSwitchRow(AcrLabels.label(AcrLED.BLUE), apply);
		switches[3] = addSwitchRow(AcrLabels.label(AcrLED.RED), apply);

		reload[0] = load("read " + getString(R.string.acr_controls_leds), reader::getLEDs, (leds) -> {
			if (leds.size() >= 2) {
				switches[0].setChecked(leds.get(0).contains(AcrLED.GREEN));
				switches[1].setChecked(leds.get(0).contains(AcrLED.RED));
				switches[2].setChecked(leds.get(1).contains(AcrLED.BLUE));
				switches[3].setChecked(leds.get(1).contains(AcrLED.RED));
			}
		}, Arrays.<View>asList(switches));
	}
}
