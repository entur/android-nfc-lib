package no.entur.android.nfc.websocket.android;

import com.acs.smartcard.ReaderException;

import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import okhttp3.WebSocket;

public class WebsocketIsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private static final String TAG = WebsocketIsoDepWrapper.class.getName();

	private WebSocket webSocket;

	public WebsocketIsoDepWrapper(WebSocket webSocket, int slotNum) {
		super(slotNum);
		this.webSocket = webSocket;
	}

	public byte[] transceive(byte[] data) {

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

	public byte[] transceiveRaw(byte[] req) throws Exception {
		throw new ReaderException();
	}
}
