package no.entur.android.nfc.external.acs.reader.bind;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.command.ACR122Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr122UCommandWrapper;

public class IAcr122UBinder extends IAcr122UReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr122UBinder.class);

	private IAcr122UCommandWrapper wrapper;

	public IAcr122UBinder() {
		attachInterface(this, IAcr122UReaderControl.class.getName());
	}

	public void setAcr122UCommands(ACR122Commands reader) {
		wrapper = new IAcr122UCommandWrapper(reader);
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
	public byte[] setBuzzerForCardDetection(boolean enable) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setBuzzerForCardDetectionAcr122U(enable);
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
