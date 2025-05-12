package no.entur.android.nfc.external.acs.reader;

import android.os.Parcelable;

import java.util.List;

import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public abstract class AcrReader extends RemoteCommandReader implements Parcelable {

	protected static final String DESCRIPTOR = "android.nfc.INfcTag";

	private static final String TAG = AcrReader.class.getName();

	public abstract String getFirmware();

	public abstract List<AcrPICC> getPICC();

	public abstract boolean setPICC(AcrPICC... types);

	public abstract byte[] control(int slotNum, int controlCode, byte[] command);

	public abstract byte[] transmit(int slotNum, byte[] command);

	protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new AcrReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new AcrReaderException(string);
	}

	public abstract byte[] power(int slotNumber, int action);

	public abstract int setProtocol(int slotNumber, int preferredProtocols);

	public abstract int getState(int slotNumber);

	public abstract int getNumberOfSlots();

}
