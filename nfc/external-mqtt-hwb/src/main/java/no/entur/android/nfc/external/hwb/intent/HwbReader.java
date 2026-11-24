package no.entur.android.nfc.external.hwb.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.external.ExternalNfcReader;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class HwbReader extends RemoteCommandReader implements ExternalNfcReader {

    protected final String id;

    public HwbReader(String deviceId, IHwbReaderControl readerControl) {
        this.id = deviceId;
        this.name = deviceId;
        this.readerControl = readerControl;
    }

    protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new HwbReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new HwbReaderException(string);
	}

    @Override
    public String getId() {
        return id;
    }

    protected IHwbReaderControl readerControl;
    
    public DiagnosticsSchema getDiagnostics(long timeout) throws HwbReaderException {
        byte[] response;
        try {
            response = readerControl.getDiagnostics(timeout);
        } catch (RemoteException e) {
            throw new HwbReaderException(e);
        }

        return readParcelable(response, DiagnosticsSchema.CREATOR);
    }

    public void setControlResult(ValidityType result, String title, String description) {
        byte[] response;
        try {
            response = readerControl.setControlResult(result.toString(), title, description);
        } catch (RemoteException e) {
            throw new HwbReaderException(e);
        }

        readVoid(response);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Creator<HwbReader> CREATOR = new Creator<HwbReader>() {
        @Override
        public HwbReader createFromParcel(Parcel in) {
            String id = in.readString();

            IBinder binder = in.readStrongBinder();

            IHwbReaderControl iin = IHwbReaderControl.Stub.asInterface(binder);

            return new HwbReader(id, iin);
        }

        @Override
        public HwbReader[] newArray(int size) {
            return new HwbReader[size];
        }
    };

}
