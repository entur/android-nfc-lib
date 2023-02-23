package no.entur.android.nfc.external.acs.reader.command.remote;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.command.ACR1283Commands;
import no.entur.android.nfc.external.acs.reader.command.acr1281.PICCOperatingParameter;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IAcr1283CommandWrapper extends AcrRemoteCommandWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1283CommandWrapper.class);

	private ACR1283Commands commands;

	public IAcr1283CommandWrapper(ACR1283Commands commands) {
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
			PICCOperatingParameter parameter = commands.getPICC(0);

			picc = parameter.getOperation() & 0xFF;
		} catch (Exception e) {
			LOGGER.debug("Problem reading PICC", e);

			exception = e;
		}

		return returnValue(picc, exception);
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

	public byte[] setPICC(int picc) {

		Boolean result = null;
		Exception exception = null;
		try {
			PICCOperatingParameter input = new PICCOperatingParameter(picc);
			PICCOperatingParameter output = commands.setPICC(0, input);

			result = output.equals(input);
		} catch (Exception e) {
			LOGGER.debug("Problem setting PICC", e);

			exception = e;
		}

		return returnValue(result, exception);

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

	public byte[] clearDisplay() {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.clearLCD(0);
		} catch (Exception e) {
			LOGGER.debug("Problem setting LEDs", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] displayText(int fontId, boolean styleBold, int line, int position, byte[] message) {

		FontSet font;
		if (fontId == 'a') {
			font = FontSet.FONT_1;
		} else if (fontId == 'b') {
			font = FontSet.FONT_2;
		} else if (fontId == 'c') {
			font = FontSet.FONT_3;
		} else {
			throw new IllegalArgumentException("Unknown font " + fontId);
		}

		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.displayText(0, font, styleBold, line, position, message);
		} catch (Exception e) {
			LOGGER.debug("Problem setting LEDs", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] lightDisplayBacklight(boolean on) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.lightBacklight(0, on);
		} catch (Exception e) {
			LOGGER.debug("Problem setting LEDs", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

	public byte[] setDisplayContrast(int contrast) {
		Boolean result = null;
		Exception exception = null;
		try {
			result = commands.setDisplayContrast(0, contrast);
		} catch (Exception e) {
			LOGGER.debug("Problem setting LEDs", e);

			exception = e;
		}

		return returnValue(result, exception);
	}

}
