package no.entur.android.nfc.external.acs.reader.bind;

import android.os.RemoteException;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.acs.reader.command.ACR1255Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1255UCommandWrapper;

public class IAcr1255UBinder extends IAcr1255UReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1255UBinder.class);

	private IAcr1255UCommandWrapper wrapper;

	public IAcr1255UBinder() {
		attachInterface(this, IAcr1255UBinder.class.getName());
	}

	public void setCommands(ACR1255Commands reader) {
		wrapper = new IAcr1255UCommandWrapper(reader);
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

	@Override
	public byte[] getSerialNumber() throws RemoteException {
		return new byte[0];
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
	public byte[] getAutoPPS() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getAutoPPS();
	}

	@Override
	public byte[] setAutoPPS(byte[] bytes) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAutoPPS(bytes[0], bytes[1]);
	}

	@Override
	public byte[] getAntennaFieldStatus() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getAntennaFieldStatus();
	}

	@Override
	public byte[] setAntennaField(boolean b) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAntennaField(b);
	}

	@Override
	public byte[] getBluetoothTransmissionPower() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getBluetoothTransmissionPower();
	}

	@Override
	public byte[] setBluetoothTransmissionPower(byte b) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setBluetoothTransmissionPower(b);
	}

	@Override
	public byte[] setSleepModeOption(byte b) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setSleepModeOption(b);
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
	public byte[] setAutomaticPolling(boolean b) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAutomaticPolling(b);
	}

	@Override
	public byte[] getBatteryLevel() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getBatteryLevel();
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
