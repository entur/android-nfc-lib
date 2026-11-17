package no.entur.android.nfc.websocket.android;

import android.os.Parcelable;

import java.io.IOException;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.websocket.messages.card.CardClient;

public class WebsocketIsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private CardClient cardClient;

	public WebsocketIsoDepWrapper(CardClient cardClient) {
		super(-1);
		this.cardClient = cardClient;
	}

	public byte[] transceive(byte[] data) throws IOException {
		return getResponse(cardClient.transcieve(data));
	}

	public byte[] transceiveRaw(byte[] req) throws IOException {
		return getResponse(cardClient.transcieve(req));
	}

	private static byte[] getResponse(byte[] response) throws IOException {
		if(response == null) {
			// server was unable to transceive, or no response within timeout
			throw new IOException("Unable to transceive");
		}
		return response;
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        throw new RuntimeException("Not implemented");
    }
}
