package no.entur.android.nfc.external.tag;

import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.external.service.tag.CommandTechnology;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.TransceiveResult;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class NfcAAdapter extends DefaultTechnology implements CommandTechnology {

	protected static final String TAG = NfcAAdapter.class.getName();

	private AbstractReaderIsoDepWrapper wrapper;
	private boolean print;

	public NfcAAdapter(int slotNumber, AbstractReaderIsoDepWrapper wrapper, boolean print) {
		super(TagTechnology.NFC_A, slotNumber);
		this.wrapper = wrapper;
		this.print = print;
	}

	public TransceiveResult transceive(byte[] data, boolean raw) throws RemoteException {
		try {
			byte[] transceive;
			if (raw) {
				if (print) {
					Log.d(TAG, "Transceive raw request " + ByteArrayHexStringConverter.toHexString(data));
				}

				transceive = transmitRaw(data);

				if (print) {
					Log.d(TAG, "Transceive raw response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			} else {
				if (print) {
					Log.d(TAG, "Transceive request " + ByteArrayHexStringConverter.toHexString(data));
				}
				transceive = transceive(data);

				if (print) {
					Log.d(TAG, "Transceive response " + ByteArrayHexStringConverter.toHexString(transceive));
				}
			}

			return new TransceiveResult(TransceiveResult.RESULT_SUCCESS, transceive);
		} catch (Exception e) {
			Log.d(TAG, "Problem sending command", e);

			return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
		}

	}

	public byte[] transmitRaw(byte[] adpu) throws Exception {
		return DESFireAdapter.responseADPUToRaw(rawToRequestADPU(adpu));
	}

	public byte[] rawToRequestADPU(byte[] commandMessage) throws Exception {
		return transceive(DESFireAdapter.wrapMessage(commandMessage[0], commandMessage, 1, commandMessage.length - 1));
	}

	/**
	 * Send a command to the card and return the response.
	 *
	 * @param command the command
	 * @throws IOException
	 * @return the PICC response
	 */
	public byte[] transceive(byte[] command) throws Exception {

		if (print) {
			Log.d(TAG, "===> " + getHexString(command, true) + " (" + command.length + ")");
		}

		byte[] response = wrapper.transceive(command);

		if (print) {
			Log.d(TAG, "<=== " + getHexString(response, true) + " (" + command.length + ")");
		}

		return response;
	}

	public static String getHexString(byte[] a, boolean space) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x", b & 0xff));
			if (space) {
				sb.append(' ');
			}
		}
		return sb.toString().trim().toUpperCase();
	}

	@Override
	public String toString() {
		return NfcA.class.getSimpleName();
	}
}
