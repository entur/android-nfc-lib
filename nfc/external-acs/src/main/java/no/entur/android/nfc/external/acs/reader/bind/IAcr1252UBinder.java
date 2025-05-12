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
import no.entur.android.nfc.external.acs.reader.command.ACR1252Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1252UCommandWrapper;

public class IAcr1252UBinder extends IAcr1252UReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1252UBinder.class);

	private IAcr1252UCommandWrapper wrapper;

	public IAcr1252UBinder() {
		attachInterface(this, IAcr1252UReaderControl.class.getName());
	}

	public void setCommands(ACR1252Commands reader) {
		wrapper = new IAcr1252UCommandWrapper(reader);
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
	public byte[] setBuzzer(boolean enable) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setBuzzer(enable);
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
