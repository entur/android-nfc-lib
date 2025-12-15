package no.entur.android.nfc.external.atr210.card;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;

public class Atr210IsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private Atr210CardCommands cardCommands;

	public Atr210IsoDepWrapper(Atr210CardCommands cardCommands) {
		super(-1);
		this.cardCommands = cardCommands;
	}

	public byte[] transceive(byte[] data) throws IOException {
		return cardCommands.transcieve(data);
	}

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transcieve(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

}
