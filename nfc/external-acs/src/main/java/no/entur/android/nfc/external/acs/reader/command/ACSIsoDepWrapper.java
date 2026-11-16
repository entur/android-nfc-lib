package no.entur.android.nfc.external.acs.reader.command;

import android.os.Parcelable;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;

public class ACSIsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private static final String TAG = ACSIsoDepWrapper.class.getName();

	private ReaderWrapper isoDep;

	public ACSIsoDepWrapper(ReaderWrapper isoDep, int slotNum) {
		super(slotNum);
		this.isoDep = isoDep;
	}

	public byte[] transceive(byte[] data) throws ReaderException {

		// Log.d(TAG, "Transceive request " + ACRCommands.toHexString(data));

		byte[] buffer = new byte[2048];

		int read = isoDep.transmit(slotNum, data, data.length, buffer, buffer.length);

		byte[] response = new byte[read];
		System.arraycopy(buffer, 0, response, 0, read);

		// Log.d(TAG, "Transceive response " + ACRCommands.toHexString(response));

		return response;
	}

	public byte[] transceiveRaw(byte[] req) throws Exception {
		// raw NFC-A commands, i.e. NfcA.transceive(..) for MIFARE Ultralight / NTAG (GET_VERSION, READ_SIG, ..)
		if (ReaderWrapper.supportsPassThrough(isoDep.getReaderName())) {
			return isoDep.transmitPassThrough(slotNum, req);
		}
		throw new ReaderException("Raw commands are not supported for this reader");
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        throw new RuntimeException("Not implemented");
    }

}
