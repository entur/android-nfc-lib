package no.entur.android.nfc.external.acs.reader;

import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.external.remote.RemoteCommandReader;

public abstract class AcrReader extends RemoteCommandReader implements Parcelable {

	protected static final String DESCRIPTOR = "android.nfc.INfcTag";

	private static final String TAG = AcrReader.class.getName();

	public abstract String getFirmware();

	public abstract List<AcrPICC> getPICC();

	public abstract boolean setPICC(AcrPICC... types);

	public abstract byte[] control(int slotNum, int controlCode, byte[] command);

	public abstract byte[] transmit(int slotNum, byte[] command);

}
