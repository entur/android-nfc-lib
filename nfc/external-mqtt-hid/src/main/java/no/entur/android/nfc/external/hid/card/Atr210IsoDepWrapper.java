package no.entur.android.nfc.external.hid.card;

import android.os.Parcelable;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import no.entur.android.nfc.external.hid.reader.Atr210ReaderService;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210IsoDepWrapper extends AbstractReaderIsoDepWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210IsoDepWrapper.class);

    private Atr210CardCommands cardCommands;
    private boolean print;

	public Atr210IsoDepWrapper(Atr210CardCommands cardCommands, boolean print) {
		super(-1);
		this.cardCommands = cardCommands;
        this.print = print && LOGGER.isInfoEnabled();
    }

	public byte[] transceive(byte[] data) throws IOException {
        if(print) LOGGER.info(" -> " +  ByteArrayHexStringConverter.toHexString(data));
        byte[] transcieve = cardCommands.transceive(data);
        if(print) LOGGER.info(" <- " +  ByteArrayHexStringConverter.toHexString(transcieve));
        return transcieve;
    }

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardCommands.transceive(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        return cardCommands.transcieve(parcelable);
    }

}
