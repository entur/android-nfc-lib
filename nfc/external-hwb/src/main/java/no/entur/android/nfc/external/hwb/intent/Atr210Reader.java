package no.entur.android.nfc.external.hwb.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.hwb.reader.IAtr210ReaderControl;

public class Atr210Reader extends HwbReader {

	protected IAtr210ReaderControl readerControl;

	public Atr210Reader(String deviceId, IAtr210ReaderControl readerControl) {
		this.name = deviceId;
		this.readerControl = readerControl;
	}

	public DiagnosticsSchema getDiagnostics() throws HwbReaderException {
		byte[] response;
		try {
			response = readerControl.getDiagnostics();
		} catch (RemoteException e) {
			throw new HwbReaderException(e);
		}

        return readParcelable(response, DiagnosticsSchema.CREATOR);
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

    public static final Creator<Atr210Reader> CREATOR = new Creator<Atr210Reader>() {
        @Override
        public Atr210Reader createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IAtr210ReaderControl iin = IAtr210ReaderControl.Stub.asInterface(binder);

            return new Atr210Reader(name, iin);
        }

        @Override
        public Atr210Reader[] newArray(int size) {
            return new Atr210Reader[size];
        }
    };

}
