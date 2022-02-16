package no.entur.android.nfc.external.acs.reader.command;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;

public class ACSIsoDepWrapper {

	private static final String TAG = ACSIsoDepWrapper.class.getName();

	private ReaderWrapper isoDep;
	private int slotNum;

	public ACSIsoDepWrapper(ReaderWrapper isoDep, int slotNum) {
		this.isoDep = isoDep;
		this.slotNum = slotNum;
	}

	public byte[] transceive(byte[] data) throws ReaderException {

		// Log.d(TAG, "Transceive request " + ACRCommands.toHexString(data));

		byte[] buffer = new byte[2048];
		int read;
		try {
			read = isoDep.transmit(slotNum, data, data.length, buffer, buffer.length);
		} catch (ReaderException e) {
			throw new ReaderException(e);
		}

		byte[] response = new byte[read];
		System.arraycopy(buffer, 0, response, 0, read);

		// Log.d(TAG, "Transceive response " + ACRCommands.toHexString(response));

		return response;
	}

	public byte[] transmitPassThrough(byte[] req) throws ReaderException {
		throw new ReaderException();
	}
}
