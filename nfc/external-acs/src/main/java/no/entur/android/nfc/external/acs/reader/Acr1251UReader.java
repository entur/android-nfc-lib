package no.entur.android.nfc.external.acs.reader;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.acs.reader.bind.IAcr1251UReaderControl;

public class Acr1251UReader extends AcrReader {

	private static final String TAG = Acr1251UReader.class.getName();

	private static final int POLL_TOPAZ = 1 << 4;
	private static final int POLL_FELICA_424K = 1 << 3;
	private static final int POLL_FELICA_212K = 1 << 2;
	private static final int POLL_ISO14443_TYPE_B = 1 << 1;
	private static final int POLL_ISO14443_TYPE_A = 1;

	protected IAcr1251UReaderControl readerControl;

	public Acr1251UReader(String name, IAcr1251UReaderControl readerControl) {
		this.name = name;
		this.readerControl = readerControl;
	}

	public String getFirmware() throws AcrReaderException {

		byte[] response;
		try {
			response = readerControl.getFirmware();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readString(response);
	}

	public List<AcrPICC> getPICC() {
		byte[] response;
		try {
			response = readerControl.getPICC();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		int operation = readInteger(response);

		ArrayList<AcrPICC> values = new ArrayList<AcrPICC>();

		if ((operation & POLL_TOPAZ) != 0) {
			values.add(AcrPICC.POLL_TOPAZ);
		}
		if ((operation & POLL_FELICA_424K) != 0) {
			values.add(AcrPICC.POLL_FELICA_424K);
		}
		if ((operation & POLL_FELICA_212K) != 0) {
			values.add(AcrPICC.POLL_FELICA_212K);
		}
		if ((operation & POLL_ISO14443_TYPE_B) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_B);
		}
		if ((operation & POLL_ISO14443_TYPE_A) != 0) {
			values.add(AcrPICC.POLL_ISO14443_TYPE_A);
		}

		return values;
	}

	public boolean setPICC(AcrPICC... types) {
		int picc = 0;
		for (AcrPICC type : types) {
			switch (type) {
			case POLL_FELICA_424K: {
				picc |= POLL_FELICA_424K;
				break;
			}
			case POLL_FELICA_212K: {
				picc |= POLL_FELICA_212K;
				break;
			}
			case POLL_TOPAZ: {
				picc |= POLL_TOPAZ;
				break;
			}
			case POLL_ISO14443_TYPE_A: {
				picc |= POLL_ISO14443_TYPE_A;
				break;
			}
			case POLL_ISO14443_TYPE_B: {
				picc |= POLL_ISO14443_TYPE_B;
				break;
			}
			default: {
				throw new IllegalArgumentException("Unexpected PICC " + type);
			}
			}
		}
		byte[] response;
		try {
			response = readerControl.setPICC(picc);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readBoolean(response);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeStrongBinder(readerControl.asBinder());
	}

	public static final Creator<Acr1251UReader> CREATOR = new Creator<Acr1251UReader>() {
		@Override
		public Acr1251UReader createFromParcel(Parcel in) {
			String name = in.readString();

			IBinder binder = in.readStrongBinder();

			IAcr1251UReaderControl iin = IAcr1251UReaderControl.Stub.asInterface(binder);

			return new Acr1251UReader(name, iin);

		}

		@Override
		public Acr1251UReader[] newArray(int size) {
			return new Acr1251UReader[size];
		}
	};

	@Override
	public byte[] control(int slotNum, int controlCode, byte[] command) {
		byte[] response;
		try {
			response = readerControl.control(slotNum, controlCode, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readByteArray(response);
	}

	@Override
	public byte[] transmit(int slotNum, byte[] command) {
		byte[] response;
		try {
			response = readerControl.transmit(slotNum, command);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readByteArray(response);
	}


	@Override
	public byte[] power(int slotNum, int action) {
		byte[] response;
		try {
			response = readerControl.power(slotNum, action);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readByteArray(response);
	}

	@Override
	public int setProtocol(int slotNum, int preferredProtocols) {
		byte[] response;
		try {
			response = readerControl.setProtocol(slotNum, preferredProtocols);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readInteger(response);
	}

	@Override
	public int getState(int slotNum) {
		byte[] response;
		try {
			response = readerControl.getState(slotNum);
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readInteger(response);
	}

	@Override
	public int getNumberOfSlots() {
		byte[] response;
		try {
			response = readerControl.getNumSlots();
		} catch (RemoteException e) {
			throw new AcrReaderException(e);
		}

		return readInteger(response);
	}
}
