package no.entur.android.nfc.external.acs.tag;

import android.os.RemoteException;
import android.util.Log;

import com.acs.smartcard.ReaderException;

import org.nfctools.mf.block.DataBlock;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;

import java.io.IOException;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.external.tag.DefaultTechnology;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class MifareUltralightAdapter extends DefaultTechnology implements CommandTechnology {

	/**
	 * Size of a MIFARE Ultralight page in bytes
	 */
	public static final int PAGE_SIZE = 4;

	protected static final String TAG = MifareUltralightAdapter.class.getName();

	private MfUlReaderWriter readerWriter;

	public MifareUltralightAdapter(int slotNumber, MfUlReaderWriter readerWriter) {
		super(TagTechnology.MIFARE_ULTRALIGHT, slotNumber);
		this.readerWriter = readerWriter;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		// Log.d(TAG, "transceive");
		if (raw) {
			return getRawTransceiveResult(data);
		}
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
			} catch (IOException e) {
				Log.d(TAG, "Problem reading blocks " + pageOffset);
				return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
			} catch (ReaderException e) {
				Log.d(TAG, "Problem reading blocks " + pageOffset);
				return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
			}
		} else if (command == 0xA2) {
			int pageOffset = data[1] & 0xFF;

			try {
				if (data.length != 5) {
					Log.d(TAG, "Problem writing block " + pageOffset + " - size too big: " + (data.length - 1));

					return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
				}
				byte[] page = new byte[data.length - 1];
				System.arraycopy(data, 0, page, 0, page.length);

				readerWriter.writeBlock(pageOffset, new DataBlock(page));

				return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, null);
			} catch (IOException e) {
				Log.d(TAG, "Problem writing block " + pageOffset);

				return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
			}
		} else {
			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}

	private TransceiveResult getRawTransceiveResult(byte[] data) {
		try {
			byte[] result = readerWriter.transmitPassthrough(data);

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, result);
		} catch (ReaderException e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}
	}

}
