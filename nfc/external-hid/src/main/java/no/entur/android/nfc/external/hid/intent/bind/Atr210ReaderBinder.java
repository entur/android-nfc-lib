package no.entur.android.nfc.external.hid.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.hid.intent.command.Atr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.hid.reader.IAtr210ReaderControl;

public class Atr210ReaderBinder extends IAtr210ReaderControl.Stub {

	private Atr210ReaderCommandsWrapper readerCommandsWrapper;

	public Atr210ReaderBinder() {
		attachInterface(this, IAtr210ReaderControl.class.getName());
	}

    public void setReaderCommandsWrapper(Atr210ReaderCommandsWrapper readerCommandsWrapper) {
        this.readerCommandsWrapper = readerCommandsWrapper;
    }

    @Override
    public byte[] getNfcReadersConfiguration(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return Atr210ReaderCommandsWrapper.noReaderException();
        }
        return readerCommandsWrapper.getNfcReadersConfiguration(timeout);
    }

    @Override
    public byte[] setNfcReadersConfiguration(byte[] value, long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return Atr210ReaderCommandsWrapper.noReaderException();
        }
        return readerCommandsWrapper.setNfcReadersConfiguration(value, timeout);
    }

    @Override
    public byte[] getNfcReaders(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return Atr210ReaderCommandsWrapper.noReaderException();
        }
        return readerCommandsWrapper.getNfcReaders(timeout);
    }

    @Override
    public byte[] setResult(boolean valid, String led, String sound) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return Atr210ReaderCommandsWrapper.noReaderException();
        }
        return readerCommandsWrapper.setResult(valid, led, sound);
    }
}
