package no.entur.android.nfc.external.acs.reader.command;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.Acr1552UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrDefaultLEDAndBuzzerBehaviour;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.AcrReaderException;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class ACR1552Commands extends ACRCommands {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACR1552Commands.class);

	public ACR1552Commands(String name, ReaderWrapper reader) {
		super(reader);
		this.name = name;
	}

	public Integer getPICC(int slot) throws AcrReaderException, ReaderException {
		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x01, 0x20, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		byte[] data = response.getData();

		return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
	}

	public boolean setPICC(int slot, int picc) throws ReaderException {
		if ((picc & 0xFFFF) != picc) {
			throw new IllegalArgumentException();
		}

		byte byte0 = (byte) (picc & 0xFF);
		byte byte1 = (byte)((picc >> 8) & 0xFF);

		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x20, 0x02, byte0, byte1, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		byte[] data = response.getData();

		return data[0] != byte0 || data[1] != byte1;
	}

	public String getFirmware(int slot) throws ReaderException {
		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x18, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, pseudo);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		String firmware = new String(response.getData(), Charset.forName("ASCII"));

		LOGGER.debug("Read firmware " + firmware);

		return firmware;
	}

	public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[] { (byte) AcrAutomaticPICCPolling.serialize(picc) });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse1552(operation);
	}

	public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x23, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse1552(operation);
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> setDefaultLEDAndBuzzerBehaviour(int slot, AcrDefaultLEDAndBuzzerBehaviour... picc) throws ReaderException {
		return Acr1252UReader.parseBehaviour(setDefaultLEDAndBuzzerBehaviour(slot, Acr1252UReader.serializeBehaviour(picc)));
	}

	public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x21, 0x01, (byte) picc };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

		return operation;
	}

	public List<AcrDefaultLEDAndBuzzerBehaviour> getDefaultLEDAndBuzzerBehaviour(int slot) throws ReaderException {
		final int operation = getDefaultLEDAndBuzzerBehaviour2(slot);

		LOGGER.debug("Read default LED and buzzer behaviour " + Integer.toHexString(operation));

		return Acr1252UReader.parseBehaviour(operation);
	}

	public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x21, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Read default LED and buzzer behaviour " + Integer.toHexString(operation));

		return operation;
	}

	public boolean setLED(int slot, int state) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x29, 0x01, (byte)state };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		int result = (response.getData()[0] & 0xFF);

		LOGGER.debug("Set LED state to " + result);

		return result == state;
	}

	public List<AcrLED> getLED(int slot) throws ReaderException {
		int operation = getLED2(slot);

		LOGGER.debug("Read LED state " + Integer.toHexString(operation));

		List<AcrLED> leds = new ArrayList<AcrLED>();

		if ((operation & Acr1552UReader.LED_BLUE) != 0) {
			leds.add(AcrLED.BLUE);
		}

		if ((operation & Acr1552UReader.LED_GREEN) != 0) {
			leds.add(AcrLED.GREEN);
		}

		return leds;
	}

	public int getLED2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x29, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int operation = response.getData()[0] & 0xFF;

		return operation;
	}

	public int[] setAutomaticCommunicationSpeed(int slot, int maxSpeed) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x24, 0x01, (byte)maxSpeed};

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		byte[] data = response.getData();

		int max = data[0] & 0xFF;
		int current = data[1] & 0xFF;

		LOGGER.debug("Read automatic communication speed " + current + "(max " + max + ")");

		return new int[]{max, current};
	}

	public int[] getAutomaticCommunicationSpeed(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x24, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		byte[] data = response.getData();

		int max = data[0] & 0xFF;
		int current = data[1] & 0xFF;

		LOGGER.debug("Read automatic communication speed " + current + "(max " + max + ")");

		return new int[]{max, current};
	}

	public int getRadioFrequencyPower(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x50, 0x00 };

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int result = response.getData()[0] & 0xFF;

		LOGGER.debug("Got radio frequency power " + result);

		return result;
	}

	public int setRadioFrequencyPower(int slot, int value) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x01, 0x50, new byte[] { (byte)value });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int result = response.getData()[0] & 0xFF;

		LOGGER.debug("Set radio frequency power " + Integer.toHexString(result));

		return result;
	}

	public boolean setBuzzerControlSingle(int slot, int duration) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[] { (byte)duration });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		final int result = response.getData()[0] & 0xFF;

		LOGGER.debug("Set buzzer control (single) " + Integer.toHexString(result));

		return result == duration;
	}

	public int[] setBuzzerControlRepeat(int slot, int onDuration, int offDuration, int repeats) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[] { (byte)onDuration, (byte)offDuration , (byte)repeats });

		CommandAPDU response = reader.control2(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with " + ByteArrayHexStringConverter.toHexString(response.getBytes()));
		}

		byte[] data = response.getData();

		int[] results = {data[0] & 0xFF, data[1] & 0xFF, data[2] & 0xFF};

		LOGGER.debug("Set buzzer control (repeat) on-duration " + Integer.toHexString(onDuration) + " off-duration " + Integer.toHexString(offDuration) + " repeats " + Integer.toHexString(repeats));

		return results;
	}
}
