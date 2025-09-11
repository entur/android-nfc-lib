package no.entur.android.nfc.websocket.messages.card;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.smartcardio.CardException;

import no.entur.android.nfc.websocket.messages.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;

public class CardServer implements NfcMessageListener {

	private final static Logger LOGGER = LoggerFactory.getLogger(CardServer.class);

	public interface Listener {

		byte[] transcieve(byte[] message) throws IOException, CardException;

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

	public void cardPresent(List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid) {
		sender.onMessage(new CardPresentMessage(technologies, atr, historicalBytes, uid));
	}

	@Override
	public void onMessage(NfcMessage message) {
		if(message instanceof CardApduRequestMessage) {
			CardApduRequestMessage cardApduRequestMessage = (CardApduRequestMessage)message;

			try {
				LOGGER.info("-> " + ByteArrayHexStringConverter.toHexString(cardApduRequestMessage.getApdu()));
				byte[] transcieve = listener.transcieve(cardApduRequestMessage.getApdu());
				LOGGER.info("<- " + ByteArrayHexStringConverter.toHexString(transcieve));
				sender.onMessage(new CardApduResponseMessage(transcieve, cardApduRequestMessage.getId()));
			} catch (Exception e) {
				LOGGER.info("Problem sending APDU", e);
				CardApduResponseMessage m = new CardApduResponseMessage(null, cardApduRequestMessage.getId());
				m.setStatus(CardApduResponseMessage.STATUS_CARD_UNABLE_TO_TRANSCIEVE);
				sender.onMessage(m);
			}
		}
	}

}
