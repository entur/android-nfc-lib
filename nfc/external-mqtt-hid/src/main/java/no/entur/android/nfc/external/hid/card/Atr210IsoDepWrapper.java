package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210IsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private Atr210CardCommands cardCommands;

	public Atr210IsoDepWrapper(Atr210CardCommands cardCommands) {
		super(-1);
		this.cardCommands = cardCommands;
	}

	public byte[] transceive(byte[] data) throws IOException {
        Log.i(getClass().getName(), " -> " +  ByteArrayHexStringConverter.toHexString(data));
        byte[] transcieve = cardCommands.transcieve(data);
        Log.i(getClass().getName(), " <- " +  ByteArrayHexStringConverter.toHexString(transcieve));
        return transcieve;
    }

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transcieve(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

}
