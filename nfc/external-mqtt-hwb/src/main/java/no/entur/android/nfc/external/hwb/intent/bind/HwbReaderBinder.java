package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.hwb.intent.command.DefaultHwbReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class HwbReaderBinder extends IHwbReaderControl.Stub {

	private DefaultHwbReaderCommandsWrapper readerCommandsWrapper;

	public HwbReaderBinder() {
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

    @Override
    public byte[] setControlResult(String validity, String title, String description) throws RemoteException {
        if (readerCommandsWrapper == null) {
            return RemoteCommandWriter.noReaderException();
        }
        return readerCommandsWrapper.setControlResult(validity, title, description);
    }


}
