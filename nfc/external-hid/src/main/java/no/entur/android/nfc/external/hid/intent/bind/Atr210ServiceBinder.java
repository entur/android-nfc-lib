package no.entur.android.nfc.external.hid.intent.bind;

import no.entur.android.nfc.external.hid.reader.IHidServiceControl;
import no.entur.android.nfc.external.hid.intent.command.HidServiceCommandsWrapper;

public class Atr210ServiceBinder extends IHidServiceControl.Stub {

	private HidServiceCommandsWrapper serviceCommandsWrapper;

	public Atr210ServiceBinder() {
		attachInterface(this, IHidServiceControl.class.getName());
	}

    public void clearReader() {
		this.serviceCommandsWrapper = null;
	}

    public void setServiceCommandsWrapper(HidServiceCommandsWrapper serviceCommandsWrapper) {
        this.serviceCommandsWrapper = serviceCommandsWrapper;
    }

    @Override
    public byte[] getReaderIds() {
        return serviceCommandsWrapper.getReaderIds();
    }

}
