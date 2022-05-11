package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;
import android.util.Log;

import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.external.tag.DefaultTechnology;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class PN532DefaultTechnology extends DefaultTechnology implements CommandTechnology {

	protected static final String TAG = PN532DefaultTechnology.class.getName();

	protected AbstractReaderIsoDepWrapper reader;
	private boolean print;

	public PN532DefaultTechnology(int tagTechnology, int slotNumber, AbstractReaderIsoDepWrapper reader, boolean print) {
		super(tagTechnology, slotNumber);

		this.reader = reader;
		this.print = print;
	}

	@Override
	public TransceiveResult transceive(byte[] data, boolean raw) {
		try {
			byte[] transceive;
			if (!raw) {
				if (print) {
					Log.d(TAG, "Transceive request " + ByteArrayHexStringConverter.toHexString(data));
				}
				transceive = reader.transceive(data);

				if (print) {
					Log.d(TAG, "Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			} else {
				if (print) {
					Log.d(TAG, "Transceive raw request " + ByteArrayHexStringConverter.toHexString(data));
				}

				transceive = reader.transceiveRaw(data);

				if (print) {
					Log.d(TAG, "Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (Exception e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}
}
