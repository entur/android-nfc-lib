package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.hwb.intent.command.DefaultAtr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.intent.command.HwbReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class DefaultAtr210ReaderBinder extends IHwbReaderControl.Stub {

	private DefaultAtr210ReaderCommandsWrapper readerCommandsWrapper;

	public DefaultAtr210ReaderBinder() {
		attachInterface(this, IHwbReaderControl.class.getName());
	}

    public void setReaderCommandsWrapper(DefaultAtr210ReaderCommandsWrapper readerCommandsWrapper) {
        this.readerCommandsWrapper = readerCommandsWrapper;
    }

    @Override
    public byte[] getDiagnostics(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.diagnostics(timeout);
    }

    @Override
    public byte[] isPresent(long timeout) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.isPresent(timeout);
    }


}
