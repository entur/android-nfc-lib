package no.entur.android.nfc.external.hid.intent;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import no.entur.android.nfc.external.ExternalNfcReader;
import no.entur.android.nfc.external.hid.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;

public class Atr210Reader extends RemoteCommandReader implements ExternalNfcReader {

	protected RemoteCommandException createRemoteCommandException(Exception e) {
		return new HidReaderException(e);
	}

	protected RemoteCommandException createRemoteCommandException(String string) {
		return new HidReaderException(string);
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

    public NfcConfiguration getNfcConfiguration(long timeout) throws HidReaderException {
        byte[] response;
        try {
            response = readerControl.getNfcReadersConfiguration(timeout);
        } catch (RemoteException e) {
            throw new HidReaderException(e);
        }

        return readParcelable(response, NfcConfiguration.CREATOR);
    }

    public NfcConfiguration setNfcConfiguration(NfcConfiguration configuration, long timeout) throws HidReaderException {
        byte[] response;
        try {
            response = readerControl.setNfcReadersConfiguration(marshall(configuration), timeout);
        } catch (RemoteException e) {
            throw new HidReaderException(e);
        }
        return readParcelable(response, NfcConfiguration.CREATOR);
    }

    public void setResult(boolean valid, LedType led, SoundType sound) {
        byte[] response;
        try {
            String ledString = (led != null && led != LedType.NONE) ? led.toString() : null;
            String soundString = (sound != null && sound != SoundType.NONE) ? sound.toString() : null;

            response = readerControl.setResult(valid, ledString, soundString);
        } catch (RemoteException e) {
            throw new HidReaderException(e);
        }
        readVoid(response);
    }

    public NfcReaders getNfcReaders(long timeout) throws HidReaderException {
        byte[] response;
        try {
            response = readerControl.getNfcReaders(timeout);
        } catch (RemoteException e) {
            throw new HidReaderException(e);
        }
        return readParcelable(response, NfcReaders.CREATOR);
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

    @Override
    public String getId() {
        return clientId + "-" + providerId;
    }
}
