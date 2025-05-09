package no.entur.android.nfc.external.acs.reader.bind;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.acs.reader.command.ACR1222Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr1222LCommandWrapper;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class IAcr1222LBinder extends IAcr1222LReaderControl.Stub {

	private static final Logger LOGGER = LoggerFactory.getLogger(IAcr1222LBinder.class);

	private IAcr1222LCommandWrapper wrapper;

	public IAcr1222LBinder() {
		attachInterface(this, IAcr1222LReaderControl.class.getName());
	}

	public void setAcr1222LCommands(ACR1222Commands reader) {
		wrapper = new IAcr1222LCommandWrapper(reader);
	}

	public void clearReader() {
		this.wrapper = null;
	}

	@Override
	public byte[] getFirmware() throws RemoteException {
		LOGGER.debug("getFirmwareAcr1222L");
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getFirmware();
	}

	@Override
	public byte[] getPICC() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.getPICC();
	}

	/*
	 * @Override public byte[] lightLED(boolean ready, boolean progress, boolean complete, boolean error) throws RemoteException { if(iAcr1222LCommandWrapper ==
	 * null) { return noReaderException(); } return iAcr1222LCommandWrapper.lightLED(ready, progress, complete, error); }
	 * 
	 * @Override public byte[] setDefaultLEDAndBuzzerBehaviours(boolean piccPollingStatusLED, boolean piccActivationStatusLED, boolean
	 * buzzerForCardInsertionOrRemoval, boolean cardOperationBlinkingLED) throws RemoteException { if(iAcr1222LCommandWrapper == null) { return
	 * noReaderException(); }
	 * 
	 * return iAcr1222LCommandWrapper.setDefaultLEDAndBuzzerBehaviours(piccPollingStatusLED, piccActivationStatusLED, buzzerForCardInsertionOrRemoval,
	 * cardOperationBlinkingLED); }
	 */

	@Override
	public byte[] setPICC(int picc) {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setPICC(picc);
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
	public byte[] setLEDs(int leds) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.setLEDs(leds);
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
	public byte[] clearDisplay() throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.clearDisplay();
	}

	@Override
	public byte[] displayText(char fontId, boolean styleBold, int line, int position, byte[] message) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.displayText(fontId, styleBold, line, position, message);
	}

	@Override
	public byte[] lightDisplayBacklight(boolean on) throws RemoteException {
		if (wrapper == null) {
			return noReaderException();
		}
		return wrapper.lightDisplayBacklight(on);
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
