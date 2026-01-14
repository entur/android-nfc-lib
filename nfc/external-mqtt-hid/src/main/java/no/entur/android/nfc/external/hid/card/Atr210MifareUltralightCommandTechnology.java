package no.entur.android.nfc.external.hid.card;

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

public class Atr210MifareUltralightCommandTechnology extends AbstractTagTechnology implements CommandTechnology {

	/**
	 * Size of a MIFARE Ultralight page in bytes
	 */

	private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MifareUltralightCommandTechnology.class);

	private final TransceiveResultExceptionMapper exceptionMapper;
    private AbstractReaderIsoDepWrapper reader;

	public Atr210MifareUltralightCommandTechnology(AbstractReaderIsoDepWrapper reader, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.MIFARE_ULTRALIGHT);
        this.reader = reader;
		this.exceptionMapper = exceptionMapper;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		int command = data[0] & 0xFF;
        if (command == 0x30) {
            try {
                int pageOffset = data[1] & 0xFF;

                // command found through trial and error
                // INS D6, C6, 96, 86 work -> X6
                // P1 for page offset
                final byte[] apdu = new byte[] { (byte) 0xFF, (byte) 0x06, (byte)pageOffset, 0, 0x10 }; //

                byte[] transceive = reader.transceive(apdu);

                // on the form FFC700 04E9F4916AAA2480644800F07FFF7FFE 9000
                // i.e. | cls | ins + 1 | 00 | page data | status

                ResponseAPDU response = new ResponseAPDU(transceive);

                if (response.isSuccess()) {

                    byte[] payload = response.getData();
                    if(payload.length == 19) {
                        byte[] content = new byte[16];
                        System.arraycopy(payload, 3, content, 0, content.length);

                        return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, content);
                    }
                    LOGGER.debug("Got data " + ByteArrayHexStringConverter.toHexString(payload));
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
