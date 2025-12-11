package no.entur.android.nfc.external.hwb.intent.bind;

import no.entur.android.nfc.external.hwb.intent.command.Atr210ServiceCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IAtr210ReaderControl;
import no.entur.android.nfc.external.hwb.reader.IHwbServiceControl;

public class Atr210ServiceBinder extends IHwbServiceControl.Stub {

	private Atr210ServiceCommandsWrapper serviceCommandsWrapper;

	public Atr210ServiceBinder() {
		attachInterface(this, IAtr210ReaderControl.class.getName());
	}

    public void clearReader() {
		this.serviceCommandsWrapper = null;
	}

    public void setServiceCommandsWrapper(Atr210ServiceCommandsWrapper serviceCommandsWrapper) {
        this.serviceCommandsWrapper = serviceCommandsWrapper;
    }

    @Override
    public byte[] discoverReaders() {
        return serviceCommandsWrapper.discoverReaders();
    }

    @Override
    public byte[] getReaderIds() {
        return serviceCommandsWrapper.getReaderIds();
    }

}
