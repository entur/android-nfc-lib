package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;

import com.acs.smartcard.UnresponsiveCardException;

import org.nfctools.mf.block.DataBlock;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.scio.Command;
import org.nfctools.scio.Response;
import org.nfctools.spi.acs.Apdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import no.entur.android.nfc.external.acs.reader.PassthroughCommandException;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
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

	// send commands other than READ and WRITE (i.e. GET_VERSION, READ_SIG) to the tag via the reader pass-through
	private final boolean passthrough;

	public AcsMifareUltralightCommandTechnology(MfUlReaderWriter readerWriter, TransceiveResultExceptionMapper exceptionMapper) {
		this(readerWriter, exceptionMapper, false);
	}

	public AcsMifareUltralightCommandTechnology(MfUlReaderWriter readerWriter, TransceiveResultExceptionMapper exceptionMapper, boolean passthrough) {
		super(TagTechnology.MIFARE_ULTRALIGHT);
		this.readerWriter = readerWriter;
		this.exceptionMapper = exceptionMapper;
		this.passthrough = passthrough;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		// LOGGER.debug("transceive");

		int command = data[0] & 0xFF;
		if (command == 0x30) {
			int pageOffset = data[1] & 0xFF;

			try {
				return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, readFourPages(pageOffset));
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
		} else if (passthrough) {
			return getRawTransceiveResult(data);
		} else {
			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}

	/**
	 * Read 4 pages, like the READ command. Near the end of the memory, the tag rolls over to page 0 (i.e. pages 40, 41, 0, 1 for a 42 page
	 * NTAG203), so read all 4 pages in one go, so that the tag (not the reader) handles the end of the memory.
	 *
	 * @param pageOffset first page
	 * @return 16 bytes
	 */

	private byte[] readFourPages(int pageOffset) throws Exception {
		try {
			Response response = readerWriter.transmit(new Command(Apdu.INS_READ_BINARY, 0x00, pageOffset, 4 * PAGE_SIZE));
			if (response != null && response.isSuccess() && response.getData() != null && response.getData().length == 4 * PAGE_SIZE) {
				return response.getData();
			}
			LOGGER.debug("Unable to read 4 pages at once from page " + pageOffset + ": " + response);
		} catch (Exception e) {
			LOGGER.debug("Unable to read 4 pages at once from page " + pageOffset, e);
		}

		// fall back to reading one page at a time, this fails at the end of the memory
		MfBlock[] readBlock = readerWriter.readBlock(pageOffset, 4);

		byte[] result = new byte[4 * PAGE_SIZE];
		for (int i = 0; i < 4; i++) {
			System.arraycopy(readBlock[i].getData(), 0, result, i * PAGE_SIZE, PAGE_SIZE);
		}
		return result;
	}

    @Override
    public ParcelableTransceiveResult transceive(ParcelableTransceive parcelable) throws RemoteException {
        throw new RuntimeException();
    }

    private TransceiveResult getRawTransceiveResult(byte[] data) {
		try {
			byte[] result = readerWriter.transmitPassthrough(data);

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, result);
		} catch (Exception e) {
			LOGGER.debug("Problem sending command", e);

			if (e instanceof PassthroughCommandException) {
				// the tag did not respond (i.e. a command it does not support), so it is in the IDLE state:
				// select it again, so that later commands (i.e. READ) still work
				selectAgain();
			}

			return exceptionMapper.mapException(e);
		}
	}

	private void selectAgain() {
		try {
			// FF 00 00 00 04 D4 4A 01 00
			readerWriter.transmit(new Command(0x00, 0x00, 0x00, ReaderWrapper.getInListPassiveTarget()));
		} catch (Exception e) {
			LOGGER.debug("Unable to select the tag again", e);
		}
	}

}
