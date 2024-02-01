package no.entur.android.nfc.websocket.messages.card;

import java.util.List;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;

public class CardServer implements NfcMessageListener {

	private interface Listener {

		byte[] transcieve(byte[] message);

	}

	private final NfcMessageListener sender;

	private Listener listener;

	public CardServer(NfcMessageListener nfcMessageListener) {
		this.sender = nfcMessageListener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void cardLost() {
		sender.onMessage(new CardLostMessage());
	}

	public void cardPresent(List<String> technologies) {
		sender.onMessage(new CardPresentMessage(technologies));
	}

	@Override
	public void onMessage(NfcMessage message) {
		if(message instanceof CardAdpuRequestMessage) {
			CardAdpuRequestMessage cardAdpuRequestMessage = (CardAdpuRequestMessage)message;

			byte[] transcieve = listener.transcieve(cardAdpuRequestMessage.getAdpu());

			sender.onMessage(new CardAdpuResponseMessage(transcieve));
		}
	}



}
