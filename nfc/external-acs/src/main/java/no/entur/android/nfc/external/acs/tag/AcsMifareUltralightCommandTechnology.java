package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;

import com.acs.smartcard.UnresponsiveCardException;

import org.nfctools.mf.block.DataBlock;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.AbstractTagTechnology;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class AcsMifareUltralightCommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	/**
	 * Size of a MIFARE Ultralight page in bytes
	 */
	public static final int PAGE_SIZE = 4;

	private static final Logger LOGGER = LoggerFactory.getLogger(AcsMifareUltralightCommandTechnology.class);

	private final MfUlReaderWriter readerWriter;
	private final TransceiveResultExceptionMapper exceptionMapper;
	public AcsMifareUltralightCommandTechnology(MfUlReaderWriter readerWriter, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.MIFARE_ULTRALIGHT);
		this.readerWriter = readerWriter;
		this.exceptionMapper = exceptionMapper;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		// LOGGER.debug("transceive");

		int command = data[0] & 0xFF;
		if (command == 0x30) {
			int pageOffset = data[1] & 0xFF;

			try {
				MfBlock[] readBlock = readerWriter.readBlock(pageOffset, 4);

				byte[] result = new byte[4 * PAGE_SIZE];

				for (int i = 0; i < 4; i++) {
					byte[] pageData = readBlock[i].getData();

					result[i * 4] = pageData[0];
					result[i * 4 + 1] = pageData[1];
					result[i * 4 + 2] = pageData[2];
					result[i * 4 + 3] = pageData[3];
				}

				return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, result);
			} catch (Exception e) {
				LOGGER.debug("Problem sending command", e);
				return exceptionMapper.mapException(e);
			}
		} else if (command == 0xA2) {
			int pageOffset = data[1] & 0xFF;

			try {
				if (data.length != 6) {
					LOGGER.debug("Problem writing block " + pageOffset + " - size too big: " + (data.length - 2));

					return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
				}
				byte[] page = new byte[data.length - 2];
				System.arraycopy(data, 2, page, 0, page.length);

				readerWriter.writeBlock(pageOffset, new DataBlock(page));

				return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, new byte[]{0x0A});
			} catch (Exception e) {
				LOGGER.debug("Problem writing block " + pageOffset);

				return exceptionMapper.mapException(e);
			}
		} else {
			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}

    @Override
    public ParcelableTransceiveResult transceive(ParcelableTransceive parcelable) throws RemoteException {
        throw new RuntimeException();
    }

    @Override
    public boolean supportsTransceiveParcelable(String className) {
        return false;
    }

    private TransceiveResult getRawTransceiveResult(byte[] data) {
		try {
			byte[] result = readerWriter.transmitPassthrough(data);

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, result);
		} catch (Exception e) {
			LOGGER.debug("Problem sending command", e);

			return exceptionMapper.mapException(e);
		}
	}

}
