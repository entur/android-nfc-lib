package no.entur.android.nfc.external.hwb.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import no.entur.android.nfc.external.hwb.reader.IHwbServiceControl;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class HwbService extends RemoteCommandReader {

    protected IHwbServiceControl serviceControl;

    public HwbService(String name, IHwbServiceControl serviceControl) {
        this.name = name;
        this.serviceControl = serviceControl;
    }

    public void discoverReaders() throws HwbReaderException {
        byte[] response;
        try {
            response = serviceControl.discoverReaders();
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
        dest.writeString(name);
        dest.writeStrongBinder(serviceControl.asBinder());
    }

    public static final Parcelable.Creator<HwbService> CREATOR = new Parcelable.Creator<HwbService>() {
        @Override
        public HwbService createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IHwbServiceControl iin = IHwbServiceControl.Stub.asInterface(binder);

            return new HwbService(name, iin);
        }

        @Override
        public HwbService[] newArray(int size) {
            return new HwbService[size];
        }
    };

    @Override
    protected RemoteCommandException createRemoteCommandException(Exception e) {
        return new HwbServiceException(e);
    }

    @Override
    protected RemoteCommandException createRemoteCommandException(String string) {
        return new HwbServiceException(string);
    }
}
