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

	/**
	 * @throws no.entur.android.nfc.websocket.messages.card.CardLostException if the card was lost
	 * @throws IOException if the server was unable to transceive, or did not respond within the timeout
	 */

	public byte[] transceive(byte[] data) throws IOException {
		return cardClient.transcieveOrThrow(data);
	}

	public byte[] transceiveRaw(byte[] req) throws IOException {
		return cardClient.transcieveOrThrow(req);
	}

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        throw new RuntimeException("Not implemented");
    }
}
