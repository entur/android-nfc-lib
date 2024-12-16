package no.entur.android.nfc.external.acs.reader.command.remote;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.command.ACR1252Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1552Commands;

public class IAcr1552UCommandWrapper extends AcrRemoteCommandWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1552UCommandWrapper.class);

	private ACR1552Commands commands;

	public IAcr1552UCommandWrapper(ACR1552Commands commands) {
		this.commands = commands;
	}

	public byte[] getFirmware() {
		String firmware = null;
		Exception exception = null;
		try {
			firmware = commands.getFirmware(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading firmware", e);

			exception = e;
		}
		return returnValue(firmware, exception);
	}

	public byte[] getPICC() {
		Integer picc = null;
		Exception exception = null;
		try {
			picc = commands.getPICC(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading PICC", e);

			exception = e;
		}
		return returnValue(picc, exception);
	}

	public byte[] setPICC(int picc) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setPICC(0, picc);
		} catch (Exception e) {
			LOGGER.debug("Problem setting PICC", e);

			exception = e;
		}
		return returnValue(result, exception);
	}

	public byte[] control(int slotNum, int controlCode, byte[] command) {
		byte[] value = null;
		Exception exception = null;
		try {
			value = commands.control(slotNum, controlCode, command);
		} catch (Exception e) {
			LOGGER.debug("Problem control", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] transmit(int slotNum, byte[] command) {
		byte[] value = null;
		Exception exception = null;
		try {
			value = commands.transmit(slotNum, command);
		} catch (Exception e) {
			LOGGER.debug("Problem transmit", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] getDefaultLEDAndBuzzerBehaviour() {
		Integer value = null;
		Exception exception = null;
		try {
			value = commands.getDefaultLEDAndBuzzerBehaviour2(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading default led and buzzer behaviour", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) {
		Boolean value = null;
		Exception exception = null;
		try {
			int response = commands.setDefaultLEDAndBuzzerBehaviour(0, parameter);

			value = response == parameter;
		} catch (Exception e) {
			LOGGER.debug("Problem reading setting default led and buzzer behaviour", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] setAutomaticPICCPolling(int picc) {
		Boolean result = null;
		Exception exception = null;
		try {
			List<AcrAutomaticPICCPolling> parse = AcrAutomaticPICCPolling.parse1552(picc);

			List<AcrAutomaticPICCPolling> serialize = commands.setAutomaticPICCPolling(0, parse.toArray(new AcrAutomaticPICCPolling[parse.size()]));

			result = parse.equals(serialize);
		} catch (Exception e) {
			LOGGER.debug("Problem setting automatic PICC", e);

			exception = e;
		}
		return returnValue(result, exception);
	}

	public byte[] getAutomaticPICCPolling() {
		Integer picc = null;
		Exception exception = null;
		try {
			List<AcrAutomaticPICCPolling> parse = commands.getAutomaticPICCPolling(0);

			picc = AcrAutomaticPICCPolling.serialize(parse.toArray(new AcrAutomaticPICCPolling[parse.size()]));
		} catch (Exception e) {
			LOGGER.debug("Problem reading automatic PICC", e);

			exception = e;
		}
		return returnValue(picc, exception);
	}

	public byte[] getLEDs() {
		Integer picc = null;
		Exception exception = null;
		try {
			picc = commands.getLED2(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading LEDs", e);

			exception = e;
		}
		return returnValue(picc, exception);
	}

	public byte[] setLEDs(int leds) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setLED(0, leds);
		} catch (Exception e) {
			LOGGER.debug("Problem setting LEDs", e);

			exception = e;
		}
		return returnValue(result, exception);
	}

	public byte[] setRadioFrequencyPower(int parameter) {
		Boolean value = null;
		Exception exception = null;
		try {
			int response = commands.setRadioFrequencyPower(0, parameter);

			value = response == parameter;
		} catch (Exception e) {
			LOGGER.debug("Problem setting radio frequency power " + parameter, e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] getRadioFrequencyPower() {
		Integer value = null;
		Exception exception = null;
		try {
			value = commands.getRadioFrequencyPower(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading radio frequency power", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] setAutomaticCommunicationSpeed(int maxSpeed) {
		Boolean value = null;
		Exception exception = null;
		try {
			int[] response = commands.setAutomaticCommunicationSpeed(0, maxSpeed);

			value = maxSpeed == (response[1] & 0xFF) ;
		} catch (Exception e) {
			LOGGER.debug("Problem setting automatic communcation speeds", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] getAutomaticCommunicationSpeed() {
		int[] value = null;
		Exception exception = null;
		try {
			value = commands.getAutomaticCommunicationSpeed(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading automatic communcation speeds", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] setBuzzerControlSingle(int duration) {
		Boolean value = null;
		Exception exception = null;
		try {
			value = commands.setBuzzerControlSingle(0, duration);
		} catch (Exception e) {
			LOGGER.debug("Problem reading setting (single) buzzer control", e);

			exception = e;
		}
		return returnValue(value, exception);
	}

	public byte[] setBuzzerControlRepeat(int onDuration, int offDuration, int repeats) {
		Boolean value = null;
		Exception exception = null;
		try {
			int[] values = commands.setBuzzerControlRepeat(0, onDuration, offDuration, repeats);

			value = values.length == 3 && values[0] == onDuration && values[1] == offDuration && values[2] == repeats;
		} catch (Exception e) {
			LOGGER.debug("Problem reading setting (repeated) buzzer control", e);

			exception = e;
		}
		return returnValue(value, exception);
	}
}
