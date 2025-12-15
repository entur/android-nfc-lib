package no.entur.android.nfc.external.atr210.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import no.entur.android.nfc.external.atr210.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.atr210.schema.NfcConfiguationResponse;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class Atr210Reader extends RemoteCommandReader {

	protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new Atr210ReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new Atr210ReaderException(string);
	}


    protected IAtr210ReaderControl readerControl;

    protected String clientId;
    protected String providerId;

    public Atr210Reader(String name, String clientId, String providerId, IAtr210ReaderControl readerControl) {
        this.name = name;
        this.clientId = clientId;
        this.providerId = providerId;
        this.readerControl = readerControl;
    }

    public NfcConfiguationResponse getNfcReadersConfiguration(long timeout) throws Atr210ReaderException {
        byte[] response;
        try {
            response = readerControl.getNfcReadersConfiguration(timeout);
        } catch (RemoteException e) {
            throw new Atr210ReaderException(e);
        }

        return readParcelable(response, NfcConfiguationResponse.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(clientId);
        dest.writeString(providerId);
        dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Creator<Atr210Reader> CREATOR = new Creator<Atr210Reader>() {
        @Override
        public Atr210Reader createFromParcel(Parcel in) {
            String name = in.readString();
            String clientId = in.readString();
            String providerId = in.readString();

            IBinder binder = in.readStrongBinder();

            IAtr210ReaderControl iin = IAtr210ReaderControl.Stub.asInterface(binder);

            return new Atr210Reader(name, clientId, providerId, iin);
        }

        @Override
        public Atr210Reader[] newArray(int size) {
            return new Atr210Reader[size];
        }
    };
}
