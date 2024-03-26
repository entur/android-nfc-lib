package no.entur.android.nfc.websocket.android;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.websocket.messages.card.CardClient;

public class WebsocketIsoDepWrapper extends AbstractReaderIsoDepWrapper {

	private CardClient cardClient;

	public WebsocketIsoDepWrapper(CardClient cardClient) {
		super(-1);
		this.cardClient = cardClient;
	}

	public byte[] transceive(byte[] data) {
		return cardClient.transcieve(data);
	}

	public byte[] transceiveRaw(byte[] req) throws Exception {
		return cardClient.transcieve(req);
	}

}
