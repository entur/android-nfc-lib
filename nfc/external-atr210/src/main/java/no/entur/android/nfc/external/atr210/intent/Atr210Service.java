package no.entur.android.nfc.external.atr210.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.List;

import no.entur.android.nfc.external.atr210.reader.IAtr210ServiceControl;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class Atr210Service extends RemoteCommandReader {

    protected IAtr210ServiceControl serviceControl;

    public Atr210Service(String name, IAtr210ServiceControl serviceControl) {
        this.name = name;
        this.serviceControl = serviceControl;
    }

    public List<String> getReaderIds() throws Atr210ReaderException {
        byte[] response;
        try {
            response = serviceControl.getReaderIds();
        } catch (RemoteException e) {
            throw new Atr210ReaderException(e);
        }

        return readStrings(response);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStrongBinder(serviceControl.asBinder());
    }

    public static final Parcelable.Creator<Atr210Service> CREATOR = new Parcelable.Creator<Atr210Service>() {
        @Override
        public Atr210Service createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IAtr210ServiceControl iin = IAtr210ServiceControl.Stub.asInterface(binder);

            return new Atr210Service(name, iin);
        }

        @Override
        public Atr210Service[] newArray(int size) {
            return new Atr210Service[size];
        }
    };

    @Override
    protected RemoteCommandException createRemoteCommandException(Exception e) {
        return new Atr210ServiceException(e);
    }

    @Override
    protected RemoteCommandException createRemoteCommandException(String string) {
        return new Atr210ServiceException(string);
    }
}
