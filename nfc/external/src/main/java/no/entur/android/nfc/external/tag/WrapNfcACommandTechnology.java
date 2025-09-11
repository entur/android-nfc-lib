package no.entur.android.nfc.external.tag;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

/**
 *
 * NFC A which wraps raw commands into apdus
 *
 * TODO can this be replaced by DefaultCommandTechnology?
 *
 */

public class WrapNfcACommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	private static final Logger LOGGER = LoggerFactory.getLogger(WrapNfcACommandTechnology.class);

	private AbstractReaderIsoDepWrapper wrapper;
	private TransceiveResultExceptionMapper mapper;
	private boolean print;

	public WrapNfcACommandTechnology(AbstractReaderIsoDepWrapper wrapper, TransceiveResultExceptionMapper mapper, boolean print) {
		super(TagTechnology.NFC_A);
		this.wrapper = wrapper;
		this.mapper = mapper;
		this.print = print;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		try {
			byte[] transceive;
			if (raw) {
				if (print) {
					LOGGER.debug("Transceive raw request " + ByteArrayHexStringConverter.toHexString(data));
				}

				transceive = transmitRaw(data);

				if (print) {
					LOGGER.debug("Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			} else {
				if (print) {
					LOGGER.debug("Transceive request " + ByteArrayHexStringConverter.toHexString(data));
				}
				transceive = transceive(data);

				if (print) {
					LOGGER.debug("Transceive response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (Exception e) {
			LOGGER.debug("Problem sending command", e);

			return mapper.mapException(e);
		}

	}

	public byte[] transmitRaw(byte[] apdu) throws Exception {
		return DESFireAdapter.responseAPDUToRaw(rawToRequestAPDU(apdu));
	}

	public byte[] rawToRequestAPDU(byte[] commandMessage) throws Exception {
		return transceive(DESFireAdapter.wrapMessage(commandMessage[0], commandMessage, 1, commandMessage.length - 1));
	}

	/**
	 * Send a command to the card and return the response.
	 *
	 * @param command the command
	 * @throws IOException
	 * @return the PICC response
	 */
	public byte[] transceive(byte[] command) throws Exception {

		if (print) {
			LOGGER.debug("===> " + ByteArrayHexStringConverter.toHexString(command) + " (" + command.length + ")");
		}

		byte[] response = wrapper.transceive(command);

		if (print) {
			LOGGER.debug("<=== " + ByteArrayHexStringConverter.toHexString(response) + " (" + command.length + ")");
		}

		return response;
	}
	@Override
	public String toString() {
		return NfcA.class.getSimpleName();
	}
}
