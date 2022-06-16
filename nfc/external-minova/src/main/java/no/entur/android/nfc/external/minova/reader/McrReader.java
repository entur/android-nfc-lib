package no.entur.android.nfc.external.minova.reader;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import no.entur.android.nfc.external.minova.IMcr0XReaderControl;

public class McrReader extends MinovaReader {

    private static final String TAG = McrReader.class.getName();

    protected IMcr0XReaderControl readerControl;

    public McrReader(String name, IMcr0XReaderControl readerControl) {
        this.readerControl = readerControl;
    }

/*    public String getFirmware() throws McrReaderException {

        byte[] response;
        try {
            response = readerControl.getFirmware();
        } catch (RemoteException e) {
            throw new McrReaderException(e);
        }

        return readString(response);
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStrongBinder(readerControl.asBinder());
    }

    public static final Creator<McrReader> CREATOR = new Creator<McrReader>() {
        @Override
        public McrReader createFromParcel(Parcel in) {
            String name = in.readString();

            IBinder binder = in.readStrongBinder();

            IMcr0XReaderControl iin = IMcr0XReaderControl.Stub.asInterface(binder);

            return new McrReader(name, iin);

        }

        @Override
        public McrReader[] newArray(int size) {
            return new McrReader[size];
        }
    };

    public void buzz(int duration, int times) {
        byte[] response;
        try {
            response = readerControl.buzz(duration, times);
        } catch (RemoteException e) {
            throw new McrReaderException(e);
        }

        readVoid(response);
    }

    public void displayText(int xAxis, int yAxis, int font, java.lang.String text) {
        byte[] response;
        try {
            response = readerControl.displayText(xAxis, yAxis, font, text);
        } catch (RemoteException e) {
            throw new McrReaderException(e);
        }

        readVoid(response);
    }

    public void displayTextDelayed(int xAxis, int yAxis, int font, java.lang.String text, int durationInMillis) {
        byte[] response;
        try {
            response = readerControl.displayTextWithDuration(xAxis, yAxis, font, text, durationInMillis);
        } catch (RemoteException e) {
            throw new McrReaderException(e);
        }

        readVoid(response);
    }

}
