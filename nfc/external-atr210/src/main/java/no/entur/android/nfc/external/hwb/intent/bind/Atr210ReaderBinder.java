package no.entur.android.nfc.external.hwb.intent.bind;

import android.os.RemoteException;

import no.entur.android.nfc.external.hwb.intent.command.Atr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class Atr210ReaderBinder extends IAtr210ReaderControl.Stub {

	private Atr210ReaderCommandsWrapper readerCommandsWrapper;

	public Atr210ReaderBinder() {
		attachInterface(this, IAtr210ReaderControl.class.getName());
	}

    public void setReaderCommandsWrapper(Atr210ReaderCommandsWrapper readerCommandsWrapper) {
        this.readerCommandsWrapper = readerCommandsWrapper;
    }


}
