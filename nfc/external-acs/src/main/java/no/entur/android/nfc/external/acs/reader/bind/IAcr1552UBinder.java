package no.entur.android.nfc.external.acs.reader.bind;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.acs.reader.command.ACR1252Commands;
import no.entur.android.nfc.external.acs.reader.command.ACR1552Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1252UCommandWrapper;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1552UCommandWrapper;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class IAcr1552UBinder extends IAcr1552UReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1552UBinder.class);

	private IAcr1552UCommandWrapper wrapper;

	public IAcr1552UBinder() {
		attachInterface(this, IAcr1252UReaderControl.class.getName());
	}

	public void setCommands(ACR1552Commands reader) {
		wrapper = new IAcr1552UCommandWrapper(reader);
	}

	public void clearReader() {
		this.wrapper = null;
	}

	public byte[] getFirmware() {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getFirmware();
	}

	private byte[] noReaderException() {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);
			dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
			dout.writeUTF("Reader not connected");

			byte[] response = out.toByteArray();

			LOGGER.debug("Send exception response length " + response.length + ":" + ByteArrayHexStringConverter.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public byte[] getPICC() {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getPICC();
	}

	public byte[] setPICC(int picc) {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setPICC(picc);
	}

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}

		return wrapper.control(slotNum, controlCode, command);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}

		return wrapper.transmit(slotNum, command);
	}

	@Override
	public byte[] getDefaultLEDAndBuzzerBehaviour() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getDefaultLEDAndBuzzerBehaviour();
	}

	@Override
	public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setDefaultLEDAndBuzzerBehaviour(parameter);
	}

	@Override
	public byte[] setBuzzerControlSingle(int duration) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setBuzzerControlSingle(duration);
	}

	@Override
	public byte[] setBuzzerControlRepeat(int onDuration, int offDuration, int repeats) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setBuzzerControlRepeat(onDuration, offDuration, repeats);
	}

	@Override
	public byte[] getAutomaticPICCPolling() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getAutomaticPICCPolling();
	}

	@Override
	public byte[] setAutomaticPICCPolling(int picc) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAutomaticPICCPolling(picc);
	}

	@Override
	public byte[] getAutomaticCommunicationSpeed() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getAutomaticCommunicationSpeed();
	}

	@Override
	public byte[] setAutomaticCommunicationSpeed(int picc) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAutomaticCommunicationSpeed(picc);
	}

	@Override
	public byte[] getRadioFrequencyPower() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getRadioFrequencyPower();
	}

	@Override
	public byte[] setRadioFrequencyPower(int power) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setRadioFrequencyPower(power);
	}

	@Override
	public byte[] getLEDs() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getLEDs();
	}

	@Override
	public byte[] setLEDs(int leds) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setLEDs(leds);
	}

	@Override
	public byte[] power(int slotNum, int action) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.power(slotNum, action);
	}

	@Override
	public byte[] setProtocol(int slotNum, int preferredProtocols) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setProtocol(slotNum, preferredProtocols);
	}

	@Override
	public byte[] getState(int slotNum) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getState(slotNum);
	}

	@Override
	public byte[] getNumSlots() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getNumSlots();
	}

}
