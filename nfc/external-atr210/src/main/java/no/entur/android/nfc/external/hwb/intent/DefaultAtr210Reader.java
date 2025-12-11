package no.entur.android.nfc.external.hwb.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;

public class DefaultAtr210Reader extends Atr210Reader {

	protected IHwbReaderControl readerControl;

	public DefaultAtr210Reader(String deviceId, IHwbReaderControl readerControl) {
		this.name = deviceId;
		this.readerControl = readerControl;
	}

	public DiagnosticsSchema getDiagnostics(long timeout) throws Atr210ReaderException {
		byte[] response;
		try {
			response = readerControl.getDiagnostics(timeout);
		} catch (RemoteException e) {
			throw new Atr210ReaderException(e);
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

    public static final Creator<DefaultAtr210Reader> CREATOR = new Creator<DefaultAtr210Reader>() {
        @Override
        public DefaultAtr210Reader createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IHwbReaderControl iin = IHwbReaderControl.Stub.asInterface(binder);

            return new DefaultAtr210Reader(name, iin);
        }

        @Override
        public DefaultAtr210Reader[] newArray(int size) {
            return new DefaultAtr210Reader[size];
        }
    };

}
