package no.entur.android.nfc.external.hwb.card;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;

public class HwbIsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private HwbCardCommands cardCommands;

	public HwbIsoDepWrapper(HwbCardCommands cardCommands) {
		super(-1);
		this.cardCommands = cardCommands;
	}

	public byte[] transceive(byte[] data) throws IOException {
		return cardCommands.transceive(data);
	}

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transceive(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transceive(parcelable);
    }

}
