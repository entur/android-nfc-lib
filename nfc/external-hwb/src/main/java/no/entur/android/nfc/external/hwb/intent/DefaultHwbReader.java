package no.entur.android.nfc.external.hwb.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.hwb.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;

public class DefaultHwbReader extends HwbReader {

	protected IHwbReaderControl readerControl;

	public DefaultHwbReader(String deviceId, IHwbReaderControl readerControl) {
		this.name = deviceId;
		this.readerControl = readerControl;
	}

	public DiagnosticsSchema getDiagnostics(long timeout) throws HwbReaderException {
		byte[] response;
		try {
			response = readerControl.getDiagnostics(timeout);
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

    public static final Creator<DefaultHwbReader> CREATOR = new Creator<DefaultHwbReader>() {
        @Override
        public DefaultHwbReader createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IHwbReaderControl iin = IHwbReaderControl.Stub.asInterface(binder);

            return new DefaultHwbReader(name, iin);
        }

        @Override
        public DefaultHwbReader[] newArray(int size) {
            return new DefaultHwbReader[size];
        }
    };

}
