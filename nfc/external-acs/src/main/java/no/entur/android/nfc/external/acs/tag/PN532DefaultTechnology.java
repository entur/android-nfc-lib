package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;
import android.util.Log;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.external.acs.reader.PassthroughCommandException;
import no.entur.android.nfc.external.acs.reader.ReaderCommandException;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class PN532DefaultTechnology extends DefaultTechnology implements CommandTechnology {

	protected static final String TAG = PN532DefaultTechnology.class.getName();

	protected ACSIsoDepWrapper reader;
	private boolean print;

	public PN532DefaultTechnology(int tagTechnology, int slotNumber, ACSIsoDepWrapper reader, boolean print) {
		super(tagTechnology, slotNumber);

		this.reader = reader;
		this.print = print;
	}

	@Override
	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
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

				transceive = reader.transmitPassThrough(data);

				if (print) {
					Log.d(TAG, "Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (PassthroughCommandException e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		} catch (ReaderCommandException e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		} catch (ReaderException e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}
}
