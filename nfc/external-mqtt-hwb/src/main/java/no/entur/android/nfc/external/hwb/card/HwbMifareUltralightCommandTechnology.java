package no.entur.android.nfc.external.hwb.card;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.ResponseAPDU;
import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.AbstractTagTechnology;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.ParcelableTransceive;
import no.entur.android.nfc.wrapper.ParcelableTransceiveResult;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class HwbMifareUltralightCommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	private static final Logger LOGGER = LoggerFactory.getLogger(HwbMifareUltralightCommandTechnology.class);

	private final TransceiveResultExceptionMapper exceptionMapper;
    private AbstractReaderIsoDepWrapper reader;

	public HwbMifareUltralightCommandTechnology(AbstractReaderIsoDepWrapper reader, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.MIFARE_ULTRALIGHT);
        this.reader = reader;
		this.exceptionMapper = exceptionMapper;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		int command = data[0] & 0xFF;
        if (command == 0x30) {
            try {
                int pageOffset = data[1] & 0xFF;

                // TODO can we use raw commands?
                final byte[] apdu = new byte[] {}; //

                byte[] transceive = reader.transceive(apdu);

                ResponseAPDU response = new ResponseAPDU(transceive);

                if (response.isSuccess()) {
                    byte[] payload = response.getData();
                    return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, payload);
                }

                return new TransceiveResult(TransceiveResult.RESULT_FAILURE, transceive);
            } catch (Exception e) {
                LOGGER.debug("Problem sending command", e);
                return exceptionMapper.mapException(e);
            }
		} else if (command == 0xA2) {
            LOGGER.warn("Write not supported");

            return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
	}

    @Override
    public ParcelableTransceiveResult transceive(ParcelableTransceive parcelable) throws RemoteException {
        throw new RuntimeException();
    }


}
