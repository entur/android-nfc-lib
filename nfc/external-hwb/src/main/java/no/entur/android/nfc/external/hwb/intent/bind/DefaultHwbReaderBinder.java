package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.hwb.intent.command.Atr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.intent.command.DefaultHwbReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.intent.command.HwbReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class DefaultHwbReaderBinder extends IHwbReaderControl.Stub {

	private DefaultHwbReaderCommandsWrapper readerCommandsWrapper;

	public DefaultHwbReaderBinder() {
		attachInterface(this, IHwbReaderControl.class.getName());
	}

    public void setReaderCommandsWrapper(DefaultHwbReaderCommandsWrapper readerCommandsWrapper) {
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
