package no.entur.android.nfc.external.hwb.intent.bind;

import no.entur.android.nfc.external.atr210.reader.IAtr210ServiceControl;
import no.entur.android.nfc.external.hwb.intent.command.Atr210ServiceCommandsWrapper;

public class Atr210ServiceBinder extends IAtr210ServiceControl.Stub {

	private Atr210ServiceCommandsWrapper serviceCommandsWrapper;

	public Atr210ServiceBinder() {
		attachInterface(this, IAtr210ServiceControl.class.getName());
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
