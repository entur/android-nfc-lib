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
import no.entur.android.nfc.external.acs.reader.command.ACR1281Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1281UCommandWrapper;

public class IAcr1281UBinder extends IAcr1281UReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1281UBinder.class);

	private volatile IAcr1281UCommandWrapper wrapper;

	public IAcr1281UBinder() {
		attachInterface(this, IAcr1281UCommandWrapper.class.getName());
	}

	public void setCommands(ACR1281Commands reader) {
		wrapper = new IAcr1281UCommandWrapper(reader);
	}

	public void clearReader() {
		this.wrapper = null;
	}

	public byte[] getFirmware() {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
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
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getPICC();
	}

	public byte[] setPICC(int picc) {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setPICC(picc);
	}

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}

		return wrapper.control(slotNum, controlCode, command);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}

		return wrapper.transmit(slotNum, command);
	}

	@Override
	public byte[] getAutomaticPICCPolling() throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getAutomaticPICCPolling();
	}

	@Override
	public byte[] setAutomaticPICCPolling(int picc) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setAutomaticPICCPolling(picc);
	}

	@Override
	public byte[] getLEDs() throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getLEDs();
	}

	@Override
	public byte[] setLEDs(int leds) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setLEDs(leds);
	}

	@Override
	public byte[] getDefaultLEDAndBuzzerBehaviour() throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getDefaultLEDAndBuzzerBehaviour();
	}

	@Override
	public byte[] getExclusiveMode() throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getExclusiveMode();
	}

	@Override
	public byte[] setDefaultLEDAndBuzzerBehaviour(int parameter) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setDefaultLEDAndBuzzerBehaviour(parameter);
	}

	@Override
	public byte[] setExclusiveMode(boolean shared) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setExclusiveMode(shared);
	}

	@Override
	public byte[] power(int slotNum, int action) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.power(slotNum, action);
	}

	@Override
	public byte[] setProtocol(int slotNum, int preferredProtocols) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setProtocol(slotNum, preferredProtocols);
	}

	@Override
	public byte[] getState(int slotNum) throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getState(slotNum);
	}

	@Override
	public byte[] getNumSlots() throws RemoteException {
		IAcr1281UCommandWrapper wrapper = this.wrapper;
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getNumSlots();
	}

}
