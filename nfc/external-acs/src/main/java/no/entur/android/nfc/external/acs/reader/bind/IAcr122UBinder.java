package no.entur.android.nfc.external.acs.reader.bind;

import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import no.entur.android.nfc.external.remote.RemoteCommandWriter;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.external.acs.reader.command.ACR122Commands;
import no.entur.android.nfc.external.acs.reader.command.remote.IAcr122UCommandWrapper;

public class IAcr122UBinder extends IAcr122UReaderControl.Stub {

	private static final String TAG = IAcr122UBinder.class.getName();

	private IAcr122UCommandWrapper iAcr122UCommandWrapper;

	public IAcr122UBinder() {
		attachInterface(this, IAcr122UReaderControl.class.getName());
	}

	public void setAcr122UCommands(ACR122Commands reader) {
		iAcr122UCommandWrapper = new IAcr122UCommandWrapper(reader);
	}

	public void clearReader() {
		this.iAcr122UCommandWrapper = null;
	}

	public byte[] getFirmware() {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}
		return iAcr122UCommandWrapper.getFirmware();
	}

	private byte[] noReaderException() {

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);
			dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
			dout.writeUTF("Reader not connected");

			byte[] response = out.toByteArray();

			Log.d(TAG, "Send exception response length " + response.length + ":" + ByteArrayHexStringConverter.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public byte[] getPICC() {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}
		return iAcr122UCommandWrapper.getPICC();
	}

	public byte[] setPICC(int picc) {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}
		return iAcr122UCommandWrapper.setPICC(picc);
	}

	@Override
	public byte[] setBuzzerForCardDetection(boolean enable) throws RemoteException {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}
		return iAcr122UCommandWrapper.setBuzzerForCardDetectionAcr122U(enable);
	}

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) throws RemoteException {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}

		return iAcr122UCommandWrapper.control(slotNum, controlCode, command);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) throws RemoteException {
		if (iAcr122UCommandWrapper == null) {
			return noReaderException();
		}

		return iAcr122UCommandWrapper.transmit(slotNum, command);
	}

}
