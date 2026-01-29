package no.entur.android.nfc.external.hwb.intent.bind;

import no.entur.android.nfc.external.hwb.intent.command.HwbServiceCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.IHwbReaderControl;
import no.entur.android.nfc.external.hwb.reader.IHwbServiceControl;

public class HwbServiceBinder extends IHwbServiceControl.Stub {

	private HwbServiceCommandsWrapper serviceCommandsWrapper;

	public HwbServiceBinder() {
		attachInterface(this, IHwbReaderControl.class.getName());
	}

    public void clearReader() {
		this.serviceCommandsWrapper = null;
	}

    public void setServiceCommandsWrapper(HwbServiceCommandsWrapper serviceCommandsWrapper) {
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
