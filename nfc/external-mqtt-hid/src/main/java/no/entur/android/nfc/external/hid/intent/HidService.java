package no.entur.android.nfc.external.hid.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.List;

import no.entur.android.nfc.external.hid.reader.IHidServiceControl;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class HidService extends RemoteCommandReader {

    protected IHidServiceControl serviceControl;

    public HidService(String name, IHidServiceControl serviceControl) {
        this.name = name;
        this.serviceControl = serviceControl;
    }

    public List<String> getReaderIds() throws HidReaderException {
        byte[] response;
        try {
            response = serviceControl.getReaderIds();
        } catch (RemoteException e) {
            throw new HidReaderException(e);
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

    public static final Parcelable.Creator<HidService> CREATOR = new Parcelable.Creator<HidService>() {
        @Override
        public HidService createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IHidServiceControl iin = IHidServiceControl.Stub.asInterface(binder);

            return new HidService(name, iin);
        }

        @Override
        public HidService[] newArray(int size) {
            return new HidService[size];
        }
    };

    @Override
    protected RemoteCommandException createRemoteCommandException(Exception e) {
        return new HidServiceException(e);
    }

    @Override
    protected RemoteCommandException createRemoteCommandException(String string) {
        return new HidServiceException(string);
    }
}
