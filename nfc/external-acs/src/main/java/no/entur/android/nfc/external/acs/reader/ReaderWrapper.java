package no.entur.android.nfc.external.acs.reader;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.ReaderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.CommandAPDU;
import no.entur.android.nfc.ResponseAPDU;
import no.entur.android.nfc.hce.protocol.ResponseApduProtocolIsoDep;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class ReaderWrapper {

	public static final boolean LOG = false;

	private static final Logger LOGGER = LoggerFactory.getLogger(ReaderWrapper.class);

	private final Reader reader;

	public ReaderWrapper(UsbManager manager) {
		if(manager == null) {
			throw new IllegalArgumentException("UsbManager must not be null");
		}
		this.reader = new Reader(manager);
	}

	public boolean isSupported(UsbDevice device) {
		if (LOG) {
			log("isSupported: " + device);
		}
		return reader.isSupported(device);
	}

	public UsbDevice getDevice() {
		UsbDevice device = reader.getDevice();

		if (LOG) {
			log("getDevice: " + device);
		}

		return device;
	}

	public void open(UsbDevice usbDevice) {
		if (LOG) {
			log("open: " + usbDevice);
		}
		reader.open(usbDevice);
	}

	/**
	 * Whether raw tag commands can be sent via the PN53x InCommunicateThru pass-through
	 * (see {@link #transmitPassThrough(int, byte[])}) for a reader. Only for readers with a PN53x (compatible) chip: the ACR122U
	 * (PN532) and the ACR1222L. Other readers do not get raw commands, which then fail without being sent; i.e. the ACR1252, ACR1255
	 * and ACR1283 do not support the pass-through (the ACR1283 answers 63 00), and it is not verified for the others.
	 *
	 * @param readerName reader name, i.e. ACS ACR122U PICC Interface or ACS ACR1222L 3S CL Reader
	 * @return true if pass-through commands are supported
	 */

	public static boolean supportsPassThrough(String readerName) {
		return readerName != null && (readerName.contains("ACR1222") || readerName.matches(".*ACR122([^0-9].*)?"));
	}

	// PN53x InListPassiveTarget, one ISO 14443-A target at 106 kbps
	private static final byte[] IN_LIST_PASSIVE_TARGET = new byte[] { (byte) 0xD4, 0x4A, 0x01, 0x00 };

	/**
	 * @return the PN53x InListPassiveTarget command (D4 4A 01 00), which selects the tag again
	 */

	public static byte[] getInListPassiveTarget() {
		return IN_LIST_PASSIVE_TARGET.clone();
	}

	/**
	 * Build a pass-through command, i.e. the PN53x InCommunicateThru (D4 42) as a pseudo-APDU.
	 *
	 * @param req tag command
	 * @return FF 00 00 00 Lc D4 42 [tag command]
	 */

	public static byte[] inCommunicateThru(byte[] req) {
		// see https://stackoverflow.com/questions/57609513/what-is-the-apdu-command-combination-for-ntag-213-tag
		byte[] sub = new byte[2 + req.length];
		// 0xD4 magic byte
		// 0x42 InCommunicateThru from PN532
		sub[0] = (byte) 0xD4;
		sub[1] = (byte) 0x42;

		System.arraycopy(req, 0, sub, 2, req.length);

		return new CommandAPDU(0xFF, 0x00, 0x00, 0x00, sub, 0, sub.length).getBytes();
	}

	/**
	 * Parse a pass-through response, i.e. D5 43 [status] [tag response] 90 00 for the PN53x InCommunicateThru.
	 *
	 * @param command the pass-through command
	 * @param responseBytes the response
	 * @return the tag response
	 * @throws ReaderCommandException if the reader did not accept the command, or did not answer with an InCommunicateThru response
	 * @throws PassthroughCommandException if the status is not 00, i.e. the tag did not respond
	 */

	public static byte[] parseInCommunicateThruResponse(byte[] command, byte[] responseBytes) {
		ResponseAPDU response = new ResponseAPDU(responseBytes);

		if (!response.isSuccess()) {
			throw new ReaderCommandException("Unable to issue command " + ByteArrayHexStringConverter.toHexString(command) + ", response "
					+ ByteArrayHexStringConverter.toHexString(responseBytes));
		}
		byte[] data = response.getData();
		if (data.length < 3 || data[0] != (byte) 0xD5 || data[1] != 0x43) {
			throw new ReaderCommandException("Unexpected response to command " + ByteArrayHexStringConverter.toHexString(command) + ", response "
					+ ByteArrayHexStringConverter.toHexString(responseBytes));
		}

		int status = data[2] & 0xFF;
		if (status != 0) {
			throw new PassthroughCommandException("Got status " + status, status);
		}

		byte[] content = new byte[data.length - 3];
		System.arraycopy(data, 3, content, 0, content.length);

		return content;
	}

	public String getReaderName() {

		String name = reader.getReaderName();

		if (LOG) {
			log("getReaderName: " + name);
		}

		return name;
	}

	public int getNumSlots() {
		int slots = reader.getNumSlots();

		if (LOG) {
			log("getNumSlots: " + slots);
		}

		return slots;
	}

	public void close() {
		if (LOG) {
			log("close");
		}

		reader.close();

	}

	public byte[] power(int slotNum, int action) throws ReaderException {
		byte[] power = reader.power(slotNum, action);

		if (LOG) {
			log("power " + slotNum + " " + action + ": " + (power != null ? ByteArrayHexStringConverter.toHexString(power) : null));
		}

		return power;
	}

	public int setProtocol(int slotNum, int preferredProtocols) throws ReaderException {
		int p = reader.setProtocol(slotNum, preferredProtocols);

		if (LOG) {
			log("setProtocol " + slotNum + " " + preferredProtocols + ": " + p);
		}

		return p;
	}

	public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {

		if (LOG) {
			log("setOnStateChangeListener: " + onStateChangeListener);
		}

		reader.setOnStateChangeListener(onStateChangeListener);
	}

	public int control(int slotNum, int controlCode, byte[] command, int length, byte[] response, int length2) throws ReaderException {

		if (LOG) {
			log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + ByteArrayHexStringConverter.toHexString(command) + " length "
					+ command.length);
		}

		int control = reader.control(slotNum, controlCode, command, length, response, length2);

		if (LOG) {
			log("control " + slotNum + " " + controlCode + " " + ByteArrayHexStringConverter.toHexString(command) + " " + length + "\n"
					+ ByteArrayHexStringConverter.toHexString(response) + " " + length2 + ": " + control);
		}

		return control;
	}

	public ResponseAPDU control(int slot, int controlCode, CommandAPDU command) throws ReaderException {
		return new ResponseAPDU(control(slot, controlCode, command.getBytes()));
	}

	public CommandAPDU control2(int slot, int controlCode, CommandAPDU command) throws ReaderException {
		return new CommandAPDU(control(slot, controlCode, command.getBytes()));
	}

	public CommandAPDU control2(int slot, int controlCode, byte[] command) throws ReaderException {
		return new CommandAPDU(control(slot, controlCode, command));
	}

	public byte[] control(int slotNum, int controlCode, byte[] command) throws ReaderException {

		byte[] response = new byte[1024];

		if (LOG) {
			log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + ByteArrayHexStringConverter.toHexString(command) + " length "
					+ command.length);
		}

		int control = reader.control(slotNum, controlCode, command, command.length, response, response.length);

		if (response.length < control) {
			throw new RuntimeException("Expected result " + response.length + " <= " + control);
		}

		if (LOG) {
			log("control - slotNum: " + slotNum + " controlCode: " + controlCode + "\nrequest: " + ByteArrayHexStringConverter.toHexString(command) + " length "
					+ command.length + "\nresponse: " + ByteArrayHexStringConverter.toHexString(response, 0, control));
		}

		byte[] in = new byte[control];

		System.arraycopy(response, 0, in, 0, control);

		return in;
	}

	public ResponseAPDU transmit(int slot, CommandAPDU command) throws ReaderException {
		return new ResponseAPDU(transmit(slot, command.getBytes()));
	}

	public CommandAPDU transmit2(int slot, CommandAPDU command) throws ReaderException {
		return new CommandAPDU(transmit(slot, command.getBytes()));
	}

	public byte[] transmit(int slotNum, byte[] command) throws ReaderException {

		byte[] response = new byte[1024];

		int transmit = reader.transmit(slotNum, command, command.length, response, response.length);

		if (LOG) {
			log("transmit - slotNum: " + slotNum + "\nrequest: " + ByteArrayHexStringConverter.toHexString(command) + " length " + command.length
					+ "\nresponse: " + ByteArrayHexStringConverter.toHexString(response, 0, Math.min(transmit, response.length)));
		}

		if (response.length < transmit) {
			throw new RuntimeException("Expected result " + response.length + " <= " + transmit);
		}

		byte[] in = new byte[transmit];

		System.arraycopy(response, 0, in, 0, transmit);

		return in;
	}

	public byte[] transmitPassThrough(int slotNumber, byte[] req) throws ReaderException {
		byte[] command = inCommunicateThru(req);

		return parseInCommunicateThruResponse(command, transmit(slotNumber, command));
	}

	public byte[] controlPassThrough(int slotNumber, int controlCode, byte[] req) throws ReaderException {
		byte[] command = inCommunicateThru(req);

		return parseInCommunicateThruResponse(command, control(slotNumber, controlCode, command));
	}

	/**
	 * Select the tag again via the pass-through (PN53x InListPassiveTarget). A tag which does not respond to a command goes to the
	 * IDLE state, and the reader does not activate it again, so i.e. READ BINARY would fail.
	 *
	 * @param slotNumber slot number
	 */

	public void selectPassThroughTarget(int slotNumber) {
		try {
			transmit(slotNumber, inListPassiveTargetCommand());
		} catch (Exception e) {
			LOGGER.debug("Unable to select the tag again", e);
		}
	}

	private static byte[] inListPassiveTargetCommand() {
		return new CommandAPDU(0xFF, 0x00, 0x00, 0x00, IN_LIST_PASSIVE_TARGET, 0, IN_LIST_PASSIVE_TARGET.length).getBytes();
	}

	public int transmit(int slotNum, byte[] command, int length, byte[] response, int responseLength) throws ReaderException {

		int transmit = reader.transmit(slotNum, command, length, response, responseLength);

		if (LOG) {
			log("transmit - slotNum: " + slotNum + "\nrequest: " + ByteArrayHexStringConverter.toHexString(command, 0, length) + " length " + command.length
					+ "\nresponse: " + ByteArrayHexStringConverter.toHexString(response, 0, Math.min(transmit, responseLength)));
		}

		if (response.length < transmit) {
			throw new RuntimeException("Expected result " + responseLength + " <= " + transmit);
		}

		return transmit;

	}

	public int getState(int slotNum) {
		int state = reader.getState(slotNum);

		if (LOG) {
			log("getState " + slotNum + ": " + state);
		}

		return state;
	}

	public byte[] getAtr(int slotNum) {

		byte[] atr = reader.getAtr(slotNum);

		if (LOG) {
			log("getState " + slotNum + ": " + (atr != null ? ByteArrayHexStringConverter.toHexString(atr) : null));
		}

		return atr;
	}

	public int getProtocol(int slotNum) {

		int protocol = reader.getProtocol(slotNum);

		if (LOG) {
			log("getProtocol " + slotNum + ": " + protocol);
		}

		return protocol;
	}

	private void log(String string) {
		LOGGER.debug(string);
	}

    public String getId() {
        return "acs-" + reader.getDevice().getDeviceId();
    }
}
