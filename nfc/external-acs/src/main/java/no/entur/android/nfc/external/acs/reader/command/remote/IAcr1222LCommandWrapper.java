package no.entur.android.nfc.external.acs.reader.command.remote;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.AcrReaderException;
import no.entur.android.nfc.external.acs.reader.command.ACR1222Commands;
import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IAcr1222LCommandWrapper extends AcrRemoteCommandWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1222LCommandWrapper.class);

	private ACR1222Commands commands;

	public IAcr1222LCommandWrapper(ACR1222Commands commands) {
		this.commands = commands;
	}

	public byte[] getFirmware() {

		String firmware = null;
		Exception exception = null;
		try {
			firmware = commands.getFirmware(0);

			LOGGER.debug("Got firmware " + firmware);

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
			picc = commands.getACR122PICC(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading PICC", e);

			exception = e;
		}

		return returnValue(picc, exception);
	}

	public byte[] setPICC(int picc) {

		Boolean success = null;
		Exception exception = null;
		try {
			success = commands.setACR122PICC(0, picc);
		} catch (Exception e) {
			LOGGER.debug("Problem setting PICC", e);

			exception = e;
		}

		return returnValue(success, exception);

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
			value = commands.setDefaultLEDAndBuzzerBehaviour(0, parameter);
		} catch (Exception e) {
			LOGGER.debug("Problem reading setting default led and buzzer behaviour", e);

			exception = e;
		}

		return returnValue(value, exception);
	}

	public byte[] setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean buzzerForCardInsertionOrRemoval,
			boolean cardOperationBlinkingLED) throws AcrReaderException {
		Boolean value = null;
		Exception exception = null;
		try {
			value = commands.setDefaultLEDAndBuzzerBehaviours(0, piccPollingStatusLED, piccActivationStatusLED, buzzerForCardInsertionOrRemoval,
					cardOperationBlinkingLED);
		} catch (Exception e) {
			LOGGER.debug("Problem setting buzzer", e);

			exception = e;
		}

		return returnValue(value, exception);
	}

	public byte[] lightLED(boolean ready, boolean progress, boolean complete, boolean error) {
		Boolean value = null;
		Exception exception = null;
		try {
			value = commands.lightLED(0, ready, progress, complete, error);
		} catch (Exception e) {
			LOGGER.debug("Problem setting light", e);

			exception = e;
		}

		return returnValue(value, exception);
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

	public byte[] displayText(char fontId, boolean styleBold, int line, int position, byte[] message) {

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

	@Override
	public ACRCommands getCommands() {
		return commands;
	}
}
