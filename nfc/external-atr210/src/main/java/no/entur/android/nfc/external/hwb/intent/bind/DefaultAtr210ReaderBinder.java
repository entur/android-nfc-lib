package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.atr210.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.hwb.intent.command.Atr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class DefaultAtr210ReaderBinder extends IAtr210ReaderControl.Stub {

	private Atr210ReaderCommandsWrapper readerCommandsWrapper;

	public DefaultAtr210ReaderBinder() {
		attachInterface(this, IAtr210ReaderControl.class.getName());
	}

    public void setReaderCommandsWrapper(Atr210ReaderCommandsWrapper readerCommandsWrapper) {
        this.readerCommandsWrapper = readerCommandsWrapper;
    }

    @Override
    public byte[] getNfcReadersConfiguration(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.getNfcReadersConfiguration(timeout);
    }

    @Override
    public byte[] setNfcReadersConfiguration(byte[] value, long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.setNfcReadersConfiguration(value, timeout);
    }

    @Override
    public byte[] getNfcReaders(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.getNfcReaders(timeout);
    }
}
