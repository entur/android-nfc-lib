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

	private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MifareUltralightCommandTechnology.class);

	private final TransceiveResultExceptionMapper exceptionMapper;
    private AbstractReaderIsoDepWrapper reader;

	public Atr210MifareUltralightCommandTechnology(AbstractReaderIsoDepWrapper reader, TransceiveResultExceptionMapper exceptionMapper) {
		super(TagTechnology.MIFARE_ULTRALIGHT);
        this.reader = reader;
		this.exceptionMapper = exceptionMapper;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
        if(raw) {
            try {
                byte[] transceive = reader.transceive(data);

                return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
            } catch (Exception e) {
                return exceptionMapper.mapException(e);
            }
        } else {
            int command = data[0] & 0xFF;
            if (command == 0x30) {
                try {
                    int pageOffset = data[1] & 0xFF;

                    // command found through trial and error
                    // INS D6, C6, 96, 86 work -> X6
                    // P1 for page offset
                    //final byte[] apdu = new byte[] { (byte) 0xFF, (byte) 0x06, (byte)pageOffset, 0, 0x10 }; //

                    // command from documentation
                    final byte[] apdu = new byte[]{(byte) 0xFF, (byte) 0x20, (byte) pageOffset, 0}; //

                    byte[] transceive = reader.transceive(apdu);

                    // on the form FFC700 04E9F4916AAA2480644800F07FFF7FFE 9000
                    // i.e. | cls | ins + 1 | block number | page data (16 bytes) | status

                    ResponseAPDU response = new ResponseAPDU(transceive);

                    if (response.isSuccess()) {

                        byte[] payload = response.getData();
                        if (payload.length == 19) {
                            byte[] content = new byte[16];
                            System.arraycopy(payload, 3, content, 0, content.length);

                            return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, content);
                        }
                    }

                    return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
                } catch (Exception e) {
                    LOGGER.debug("Problem reading pages", e);
                    return exceptionMapper.mapException(e);
                }
            } else if (command == 0xA2) {

                int pageOffset = data[1] & 0xFF;

                try {
                    if (data.length != 6) {
                        LOGGER.debug("Problem writing block " + pageOffset + " - size incorrect: " + (data.length - 2));

                        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
                    }

                    final byte[] apdu = new byte[]{(byte) 0xFF, (byte) 0x22, (byte) pageOffset, 0, 0x04,
                            data[2], data[3], data[4], data[5]
                    };

                    byte[] transceive = reader.transceive(apdu);

                    // on the form
                    // | cls | ins + 1 | block number | status

                    ResponseAPDU response = new ResponseAPDU(transceive);
                    if (response.isSuccess()) {
                        return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, new byte[]{0x0A});
                    }
                    return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
                } catch (Exception e) {
                    LOGGER.debug("Problem writing block " + pageOffset);

                    return exceptionMapper.mapException(e);
                }
            }
            return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
        }
	}

    @Override
    public ParcelableTransceiveResult transceive(ParcelableTransceive parcelable) throws RemoteException {
        throw new RuntimeException();
    }


}
