package no.entur.android.nfc.external.acs.reader.command;

import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.Acr1281UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.command.acr1281.CardInsertionCounters;
import no.entur.android.nfc.external.acs.reader.command.acr1281.ExclusiveModeConfiguration;
import no.entur.android.nfc.external.acs.reader.command.acr1281.PICCOperatingParameter;
import no.entur.android.nfc.CommandAPDU;

public class ACR1281Commands extends ACRCommands {

	public static final int LED_GREEN = 1 << 1;
	public static final int LED_RED = 1;

	private static final Logger LOGGER = LoggerFactory.getLogger(ACR1281Commands.class);

	public ACR1281Commands(String name, ReaderWrapper reader) {
		super(reader);
		this.name = name;
	}

	public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {

		for (AcrAutomaticPICCPolling p : picc) {
			if (p == AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED) {
				throw new IllegalArgumentException();
			}
		}

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[] { (byte) AcrAutomaticPICCPolling.serialize(picc) });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse(operation);
	}

	public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x23, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse(operation);
	}

	public PICCOperatingParameter setPICC(int slot, PICCOperatingParameter picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x20, new byte[] { (byte) picc.getOperation() });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Set PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc.getOperation()) + ")");

		return new PICCOperatingParameter(operation);
	}

	public PICCOperatingParameter getPICC(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x20, 0x0 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Read PICC " + Integer.toHexString(operation));

		return new PICCOperatingParameter(operation);
	}

	public String getFirmware(int slot) throws ReaderException {
		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x18, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		String firmware = new String(response.getData(), Charset.forName("ASCII"));

		LOGGER.debug("Read firmware " + firmware);

		return firmware;
	}

	public boolean setLED(int slot, int state) throws ReaderException {

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x29, new byte[] { (byte) (state) });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Set LED state to " + (0xFF & response.getData()[0]));

		return true;
	}

	public List<AcrLED> getLED(int slot) throws ReaderException {
		int operation = getLED2(slot);

		LOGGER.debug("Read LED state " + Integer.toHexString(operation));

		List<AcrLED> leds = new ArrayList<AcrLED>();

		if ((operation & LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}

		if ((operation & LED_RED) != 0) {
			leds.add(AcrLED.RED);
		}

		return leds;

	}

	public int getLED2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x29, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return operation;
	}

	public void setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws ReaderException {
		if ((duration & 0xFF) != duration)
			throw new RuntimeException();

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[] { (byte) duration });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		if (operation != 0x00) {
			throw new IllegalArgumentException();
		}
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> setDefaultLEDAndBuzzerBehaviour(int slot, AcrDefaultLEDAndBuzzerBehaviour... picc) throws ReaderException {
		return Acr1281UReader.parseBehaviour(setDefaultLEDAndBuzzerBehaviour(slot, Acr1281UReader.serializeBehaviour(picc)));
	}

	public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[] { (byte) picc });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

		return operation;
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour(int slot) throws ReaderException {
		final int operation = getDefaultLEDAndBuzzerBehaviour2(slot);

		LOGGER.debug("Read default LED and buzzer behaviour " + Integer.toHexString(operation));

		return Acr1281UReader.parseBehaviour(operation);
	}

	public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x21, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Read default LED and buzzer behaviour " + Integer.toHexString(operation));

		return operation;
	}

	public CardInsertionCounters setCardsInsertionCounters(int slot, CardInsertionCounters counters) throws ReaderException {

		byte[] data = new byte[4];

		int iccCount = counters.getIccCount();
		int piccCount = counters.getPiccCount();

		data[0] = (byte) (iccCount & 0xFF); // ICC LSB
		data[1] = (byte) ((iccCount >>> 8) & 0xFF); // ICC MSB

		data[2] = (byte) (piccCount & 0xFF); // PICC LSB
		data[3] = (byte) ((piccCount >>> 8) & 0xFF); // PICC MSB

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x09, data);

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Set cards insertion counters " + counters.getIccCount() + " / " + counters.getPiccCount());

		return parseCardInsertionCounters(data);
	}

	private CardInsertionCounters parseCardInsertionCounters(byte[] data) {
		int iccCount = (data[0] & 0xFF) + ((data[1] & 0xFF) << 8);
		int piccCount = (data[2] & 0xFF) + ((data[3] & 0xFF) << 8);

		CardInsertionCounters result = new CardInsertionCounters();
		result.setIccCount(iccCount);
		result.setPiccCount(piccCount);

		return result;
	}

	public CardInsertionCounters getCardInsertionCounters(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x09, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		CardInsertionCounters cardsInsertionCounters = parseCardInsertionCounters(response.getData());

		LOGGER.debug("Set cards insertion counters " + cardsInsertionCounters);

		return cardsInsertionCounters;
	}

	public boolean manualPICCPolling(int slot) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x22, new byte[] { 0x0A });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		int result = response.getData()[0] & 0xFF;

		LOGGER.debug("Manual PICC Polling " + Integer.toHexString(result));

		return result == 0x00;
	}

	public ExclusiveModeConfiguration setExclusiveMode(int slot, ExclusiveModeConfiguration.ExclusiveMode mode) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x2B, new byte[] { (byte) mode.getValue() });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 2)) {
			throw new IllegalArgumentException();
		}

		return new ExclusiveModeConfiguration(response.getData());
	}

	public ExclusiveModeConfiguration getExclusiveMode(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x2B, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 2)) {
			throw new IllegalArgumentException();
		}

		return new ExclusiveModeConfiguration(response.getData());
	}

}
