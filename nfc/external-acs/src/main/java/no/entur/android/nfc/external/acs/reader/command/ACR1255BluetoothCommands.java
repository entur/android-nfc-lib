package no.entur.android.nfc.external.acs.reader.command;

import static no.entur.android.nfc.external.acs.reader.command.ACRCommands.isSuccess;
import static no.entur.android.nfc.external.acs.reader.command.ACRCommands.isSuccessForP2;

import android.util.Log;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.acs.bluetooth.BluetoothReader;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrLED;
import no.entur.android.nfc.external.acs.reader.ReaderCommandException;
import no.entur.android.nfc.CommandAPDU;

public class ACR1255BluetoothCommands
		implements ACR1255Commands, BluetoothReader.OnResponseApduAvailableListener, BluetoothReader.OnEscapeResponseAvailableListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACR1255BluetoothCommands.class);

	private volatile CountDownLatch latch;
	private volatile byte[] in;

	private BluetoothReader reader;
	private String name;

	public ACR1255BluetoothCommands(String name, BluetoothReader reader) {
		this.name = name;
		this.reader = reader;
	}

	public List<AcrAutomaticPICCPolling> setAutomaticPICCPolling(int slot, AcrAutomaticPICCPolling... picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x23, new byte[] { (byte) AcrAutomaticPICCPolling.serialize(picc) });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse(operation);
	}

	public List<AcrAutomaticPICCPolling> getAutomaticPICCPolling(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x23, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return AcrAutomaticPICCPolling.parse(operation);
	}

	public Boolean setPICC(int slot, int picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x20, new byte[] { (byte) picc });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		final int operation = response.getData()[0] & 0x1F; // 5 bit

		if (operation != picc) {
			LOGGER.warn("Unable to properly update PICC: Expected " + Integer.toHexString(picc) + " got " + Integer.toHexString(operation));

			return Boolean.FALSE;
		} else {
			LOGGER.debug("Updated PICC " + Integer.toHexString(operation) + " (" + Integer.toHexString(picc) + ")");

			return Boolean.TRUE;
		}
	}

	public Integer getPICC(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x20, 0x0 };

		CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0x1F; // 5 bit

		LOGGER.debug("Read PICC " + Integer.toHexString(operation));

		return operation;
	}

	public String getFirmware(int slot) throws ReaderException {
		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x18, 0x00 };

		CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, pseudo));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		String firmware = new String(response.getData(), Charset.forName("ASCII"));

		LOGGER.debug("Read firmware " + firmware);

		return firmware;
	}

	public String getSerialNumber(int slot) throws ReaderException {
		byte[] pseudo = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x47, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(pseudo));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		String firmware = new String(response.getData(), Charset.forName("ASCII"));

		LOGGER.debug("Read serial number " + firmware);

		return firmware;
	}

	/**
	 * Control the current state of the LEDs.
	 *
	 * @param slot
	 * @return
	 * @throws ReaderException
	 */

	public boolean setLED(int slot, int state) throws ReaderException {

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x29, new byte[] { (byte) (state) });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		LOGGER.debug("Set LED state to " + (0xFF & response.getData()[0]));

		return true;
	}

	public List<Set<AcrLED>> getLED(int slot) throws ReaderException {
		int operation = getLED2(slot);

		LOGGER.debug("Read LED state " + Integer.toHexString(operation));

		Set<AcrLED> first = new HashSet<AcrLED>();
		Set<AcrLED> second = new HashSet<AcrLED>();

		if ((operation & LED_1_GREEN) != 0) {
			first.add(AcrLED.GREEN);
		}

		if ((operation & LED_1_RED) != 0) {
			first.add(AcrLED.RED);
		}

		if ((operation & LED_2_BLUE) != 0) {
			second.add(AcrLED.BLUE);
		}

		if ((operation & LED_2_RED) != 0) {
			second.add(AcrLED.RED);
		}

		return Arrays.asList(first, second);

	}

	public int getLED2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x29, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		return operation;
	}

	public void setBuzzerBeepDurationOnCardDetection(int slot, int duration) throws ReaderException {
		if ((duration & 0xFF) != duration) {
			throw new RuntimeException();
		}

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x28, new byte[] { (byte) duration });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response, 1)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		if (operation != 0x00) {
			throw new IllegalArgumentException();
		}
	}

	public int setDefaultLEDAndBuzzerBehaviour(int slot, int picc) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x21, new byte[] { (byte) picc });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Set default LED and buzzer behaviour " + Integer.toHexString(operation) + " (" + picc + ")");

		return operation;
	}

	public int getDefaultLEDAndBuzzerBehaviour2(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x21, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		final int operation = response.getData()[0] & 0xFF;

		LOGGER.debug("Read default LED and buzzer behaviour " + Integer.toHexString(operation));

		return operation;
	}

	public byte getAntennaFieldStatus(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x25, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		byte data = response.getData()[0];

		LOGGER.debug("Read antenna field status " + Integer.toHexString(data & 0xFF));

		return data;
	}

	public boolean setAntennaField(int slot, boolean on) throws ReaderException {
		byte b = (byte) (on ? 0x01 : 0x00);

		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x25, new byte[] { b });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		boolean result = response.getData()[0] == 0x01;

		if (result == on) {
			LOGGER.warn("Unable to properly update antenna field: Expected " + on + " got " + result);

			return Boolean.FALSE;
		} else {
			LOGGER.debug("Updated antenna field to " + result);

			return Boolean.TRUE;
		}
	}

	public byte getBluetoothTransmissionPower(int slot) throws ReaderException {
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x50, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		byte b = response.getData()[0];

		LOGGER.debug("Read bluetooth tx power " + Integer.toHexString(b & 0xFF));

		return b;
	}

	public boolean setBluetoothTransmissionPower(int slot, byte power) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x49, new byte[] { power });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		if (response.getData()[0] == power) {
			LOGGER.warn("Unable to update bluetoth transmission power: Expected " + Integer.toHexString(power & 0xFF) + " got "
					+ Integer.toHexString(response.getData()[0] & 0xFF));

			return Boolean.FALSE;
		} else {
			LOGGER.debug("Updated bluetoth transmission power to " + Integer.toHexString(response.getData()[0] & 0xFF));

			return Boolean.TRUE;
		}
	}

	public byte[] setAutoPPS(int slot, byte tx, byte rx) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x24, new byte[] { tx, rx });

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		LOGGER.debug("Updated auto PPS " + ByteArrayHexStringConverter.toHexString(response.getData()));

		return response.getData();
	}

	public byte[] getAutoPPS(int slot) throws ReaderException {
		CommandAPDU command = new CommandAPDU(0xE0, 0x00, 0x00, 0x24);

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		LOGGER.debug("Read auto PPS " + ByteArrayHexStringConverter.toHexString(response.getData()));

		return response.getData();
	}

	public boolean setSleepModeOption(int slot, byte option) throws ReaderException {
		if (option < 0 || option > 4) {
			throw new RuntimeException();
		}
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x48, option };

		CommandAPDU response = new CommandAPDU(control(slot, Reader.IOCTL_CCID_ESCAPE, command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException("Card responded with error code");
		}

		if (response.getData()[0] == option) {
			LOGGER.warn("Unable to set sleep mode option: Expected " + Integer.toHexString(option & 0xFF) + " got "
					+ Integer.toHexString(response.getData()[0] & 0xFF));

			return Boolean.FALSE;
		} else {
			LOGGER.debug("Set sleep mode option " + Integer.toHexString(response.getData()[0] & 0xFF));

			return Boolean.TRUE;
		}
	}

	public CommandAPDU control(int slotNum, int controlCode, CommandAPDU request) throws ReaderException {
		return new CommandAPDU(control(slotNum, controlCode, request.getBytes()));
	}

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] request) throws ReaderException {
		LOGGER.debug("Raw control request: " + ByteArrayHexStringConverter.toHexString(request));
		try {

			long time = System.currentTimeMillis();

			this.in = null;
			this.latch = new CountDownLatch(1);

			reader.setOnEscapeResponseAvailableListener(this);
			if (!reader.transmitEscapeCommand(request)) {
				throw new ReaderException("Unable to transmit escape command");
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ReaderException("Problem waiting for response", e);
			}

			LOGGER.debug("Raw control response: " + ByteArrayHexStringConverter.toHexString(in) + " in " + (System.currentTimeMillis() - time) + " millis");

			return in;
		} catch (Exception e) {
			throw new ReaderException(e);
		}
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) throws ReaderException {
		return transmit(command);
	}

	public byte[] transmit(byte[] request) {
		LOGGER.debug("Raw transmit request: " + ByteArrayHexStringConverter.toHexString(request));
		try {

			in = null;
			latch = new CountDownLatch(1);

			reader.setOnResponseApduAvailableListener(this);
			if (!reader.transmitApdu(request)) {
				throw new ReaderException("Unable to transmit ADPU");
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();

				throw new ReaderException("Problem waiting for response", e);
			}

			LOGGER.debug("Raw transmit response: " + ByteArrayHexStringConverter.toHexString(in));

			return in;
		} catch (Exception e) {
			throw new ReaderCommandException(e);
		}
	}

	@Override
	public void onResponseApduAvailable(BluetoothReader bluetoothReader, byte[] apdu, int errorCode) {
		LOGGER.debug("onResponseApduAvailable: " + getResponseString(apdu, errorCode));

		if (errorCode == BluetoothReader.ERROR_SUCCESS) {
			this.in = apdu;

		}
		latch.countDown();
	}

	@Override
	public void onEscapeResponseAvailable(BluetoothReader bluetoothReader, byte[] bytes, int errorCode) {
		LOGGER.debug("onEscapeResponseAvailable: " + getResponseString(bytes, errorCode));

		if (errorCode == BluetoothReader.ERROR_SUCCESS) {
			this.in = bytes;

		}
		latch.countDown();
	}

	public String getName() {
		return name;
	}

	public boolean setAutomaticPolling(int slot, boolean on) throws ReaderException {
		byte b = (byte) (on ? 0x01 : 0x00);

		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x40, b };

		byte[] response = control(slot, Reader.IOCTL_CCID_ESCAPE, command);

		if (!isSuccessForP2(response, 0x40)) {
			throw new IllegalArgumentException("Card responded with error code " + ByteArrayHexStringConverter.toHexString(response));
		}

		// e1 00 00 40 01
		boolean result = response[4] == 0x01;

		if (result != on) {
			LOGGER.warn("Unable to properly enable/disable automatic polling: Expected " + on + " got " + result);

			return Boolean.FALSE;
		} else {
			LOGGER.debug("Updated automatic polling to " + result);

			return Boolean.TRUE;
		}
	}

	public int getBatteryLevel(int slot) throws ReaderException {
		// This is only applicable to firmware version 2.03.xx and above, and when the reader is in Bluetooth mode.
		byte[] command = new byte[] { (byte) 0xE0, 0x00, 0x00, 0x52, 0x00 };

		CommandAPDU response = control(slot, Reader.IOCTL_CCID_ESCAPE, new CommandAPDU(command));

		if (!isSuccess(response)) {
			throw new IllegalArgumentException();
		}

		byte[] data = response.getData();
		if (data.length > 0) {
			return data[0] & 0xFF;
		}
		LOGGER.debug("Unable to read battery level");

		return -1;
	}

	/* Get the Error string. */
	private static String getErrorString(int errorCode) {
		if (errorCode == BluetoothReader.ERROR_SUCCESS) {
			return "";
		} else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
			return "The checksum is invalid.";
		} else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
			return "The data length is invalid.";
		} else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
			return "The command is invalid.";
		} else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
			return "The command ID is unknown.";
		} else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
			return "The card operation failed.";
		} else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
			return "Authentication is required.";
		} else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
			return "The battery is low.";
		} else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
			return "Error characteristic is not found.";
		} else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
			return "Write command to reader is failed.";
		} else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
			return "Timeout.";
		} else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
			return "Authentication is failed.";
		} else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
			return "Undefined error.";
		} else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
			return "Received data error.";
		} else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
			return "The command failed.";
		}
		return "Unknown error.";
	}

	/* Get the Response string. */
	public static String getResponseString(byte[] response, int errorCode) {
		if (errorCode == BluetoothReader.ERROR_SUCCESS) {
			StringBuilder builder = new StringBuilder();
			if (response != null && response.length > 0) {
				builder.append(ByteArrayHexStringConverter.toHexString(response));
				builder.append(' ');
			}
			builder.append("Success");

			return builder.toString();
		}
		return getErrorString(errorCode);
	}

}
