package no.entur.android.nfc.websocket.messages.card;

import java.util.List;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderBeginPollingRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderBeginPollingResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderConnectRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderConnectResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderDisconnectRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderDisconnectResponseMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderEndPollingRequestMessage;
import no.entur.android.nfc.websocket.messages.reader.ReaderEndPollingResponseMessage;

public class CardClient implements NfcMessageListener {

	public interface Listener {

		void onCardLost();

		void onCardPresent(List<String> technologies);
	}

	private final RequestResponseMessages messages;

	private long timeout;

	private Listener listener;

	public CardClient(RequestResponseMessages messages, long timeout) {
		this.messages = messages;
		this.timeout = timeout;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public byte[] transcieve(byte[] message) {
		NfcMessage response = messages.sendAndWaitForResponse(new CardAdpuRequestMessage(message), timeout);
		
		if(response != null) {
			CardAdpuResponseMessage result = (CardAdpuResponseMessage)response;
			if(result.getStatus() == 0) {
				return result.getAdpu();
			}
		}
		return null;
	}

	@Override
	public void onMessage(NfcMessage message) {
		if(message instanceof CardLostMessage) {
			listener.onCardLost();
		} else if(message instanceof CardPresentMessage) {
			CardPresentMessage cardPresentMessage = (CardPresentMessage)message;
			listener.onCardPresent(cardPresentMessage.getTechnologies());
		}
	}


}
