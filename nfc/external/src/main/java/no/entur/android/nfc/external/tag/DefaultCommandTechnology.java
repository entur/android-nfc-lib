package no.entur.android.nfc.external.tag;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class DefaultCommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommandTechnology.class);

	protected AbstractReaderIsoDepWrapper reader;
	private boolean print;

	private TransceiveResultExceptionMapper exceptionMapper;

	public DefaultCommandTechnology(int tagTechnology, AbstractReaderIsoDepWrapper reader, boolean print, TransceiveResultExceptionMapper exceptionMapper) {
		super(tagTechnology);

		this.reader = reader;
		this.print = print;
		this.exceptionMapper = exceptionMapper;
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

			return exceptionMapper.mapException(e);
		}
	}

    @Override
    public ParcelableTransceiveResult transceive(ParcelableTransceive parcelable) throws RemoteException {
        throw new RuntimeException();
    }
}
