package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.DefaultTechnology;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class PN532DefaultTechnology extends DefaultTechnology implements CommandTechnology {

	private static final Logger LOGGER = LoggerFactory.getLogger(PN532DefaultTechnology.class);

	protected AbstractReaderIsoDepWrapper reader;
	private boolean print;

	public PN532DefaultTechnology(int tagTechnology, AbstractReaderIsoDepWrapper reader, boolean print) {
		super(tagTechnology);

		this.reader = reader;
		this.print = print;
	}

	@Override
	public TransceiveResult transceive(byte[] data, boolean raw) {
		try {
			byte[] transceive;
			if (!raw) {
				if (print) {
					LOGGER.debug("Transceive request " + ByteArrayHexStringConverter.toHexString(data));
				}
				transceive = reader.transceive(data);

				if (print) {
					LOGGER.debug("Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			} else {
				if (print) {
					LOGGER.debug("Transceive raw request " + ByteArrayHexStringConverter.toHexString(data));
				}

				transceive = reader.transceiveRaw(data);

				if (print) {
					LOGGER.debug("Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (Exception e) {
			LOGGER.debug("Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}
}
