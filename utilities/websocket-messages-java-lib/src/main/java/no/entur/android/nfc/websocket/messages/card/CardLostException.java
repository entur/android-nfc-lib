package no.entur.android.nfc.websocket.messages.card;

import java.io.IOException;

/**
 * The card was lost, i.e. it did not respond to a command. Like with Android's own NFC reader,
 * where a card which does not respond is lost (TagLostException) and must be presented again.
 */

public class CardLostException extends IOException {

	public CardLostException(String message) {
		super(message);
	}
}
