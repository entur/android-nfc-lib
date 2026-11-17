package no.entur.android.nfc.websocket.messages.card;

import java.io.IOException;
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

		void onCardPresent(CardClient client, List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid);
	}

	private final RequestResponseMessages messages;

	// set by the tag technology timeout, i.e. from another thread
	private volatile long timeout;

	private volatile Listener listener;

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

	/**
	 * @param message command
	 * @return the response, or null if the server was unable to transceive (including if the card was lost)
	 * or did not respond within the timeout
	 */

	public byte[] transcieve(byte[] message) {
		try {
			return transcieveOrThrow(message);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param message command
	 * @return the response
	 * @throws CardLostException if the card was lost, i.e. it did not respond to the command
	 * @throws IOException if the server was unable to transceive, or did not respond within the timeout
	 */

	public byte[] transcieveOrThrow(byte[] message) throws IOException {
		NfcMessage response = messages.sendAndWaitForResponse(new CardApduRequestMessage(message), timeout);
		if(response == null) {
			throw new IOException("No response within " + timeout + "ms");
		}
		CardApduResponseMessage result = (CardApduResponseMessage)response;
		if(result.getStatus() == CardApduResponseMessage.STATUS_OK) {
			return result.getApdu();
		}
		if(result.getStatus() == CardApduResponseMessage.STATUS_CARD_LOST) {
			throw new CardLostException("Card lost");
		}
		throw new IOException("Unable to transceive, status " + result.getStatus());
	}

	@Override
	public void onMessage(NfcMessage message) {
		Listener listener = this.listener; // defensive copy, might be removed when polling ends
		if(listener == null) {
			return;
		}
		if(message instanceof CardLostMessage) {
			listener.onCardLost();
		} else if(message instanceof CardPresentMessage) {
			CardPresentMessage cardPresentMessage = (CardPresentMessage)message;
			listener.onCardPresent(this, cardPresentMessage.getTechnologies(), cardPresentMessage.getAtr(), cardPresentMessage.getHistoricalBytes(), cardPresentMessage.getUid());
		}
	}


}
