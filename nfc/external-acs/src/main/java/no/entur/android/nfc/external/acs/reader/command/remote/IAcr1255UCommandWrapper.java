package no.entur.android.nfc.external.acs.reader.command.remote;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.command.ACR1255Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1255UsbCommands;
import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IAcr1255UCommandWrapper extends AcrRemoteCommandWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1255UCommandWrapper.class);

	private ACR1255Commands commands;

	public IAcr1255UCommandWrapper(ACR1255Commands commands) {
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
			List<AcrAutomaticPICCPolling> parse = AcrAutomaticPICCPolling.parse(picc);

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

	public byte[] getAutoPPS() {
		byte[] picc = null;
		Exception exception = null;
		try {
			picc = commands.getAutoPPS(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading auto PPS", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] setAutoPPS(byte tx, byte rx) {
		byte[] picc = null;
		Exception exception = null;
		try {
			picc = commands.setAutoPPS(0, tx, rx);
		} catch (Exception e) {
			LOGGER.debug("Problem reading LEDs", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] getAntennaFieldStatus() {
		Byte picc = null;
		Exception exception = null;
		try {
			picc = commands.getAntennaFieldStatus(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading antenna field status", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] setAntennaField(boolean b) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setAntennaField(0, b);
		} catch (Exception e) {
			LOGGER.debug("Problem setting antenna field", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] getBluetoothTransmissionPower() {
		Byte picc = null;
		Exception exception = null;
		try {
			picc = commands.getBluetoothTransmissionPower(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading bluetooth transmission power", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] setBluetoothTransmissionPower(byte b) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setBluetoothTransmissionPower(0, b);
		} catch (Exception e) {
			LOGGER.debug("Problem setting bluetooth transmission power", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] setSleepModeOption(byte b) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setSleepModeOption(0, b);
		} catch (Exception e) {
			LOGGER.debug("Problem setting sleep mode", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] setAutomaticPolling(boolean b) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setAutomaticPolling(0, b);
		} catch (Exception e) {
			LOGGER.debug("Problem enabling/disabling automatic polling", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] getBatteryLevel() {
		Integer level = null;
		Exception exception = null;
		try {
			level = commands.getBatteryLevel(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading battery level", e);

			exception = e;
		}

		return returnValue(level, exception);
	}

	@Override
	public ACRCommands getCommands() {
		if(commands instanceof ACRCommands) {
			return (ACRCommands) commands;
		}
		throw new IllegalArgumentException("USB commands not supported for bluetooth reader");
	}
}
