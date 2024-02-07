package no.entur.android.nfc.websocket.server.readers;

import javax.smartcardio.CardException;

public class AcsCardException extends CardException {

    public AcsCardException(String message) {
        super(message);
    }

    public AcsCardException(Throwable cause) {
        super(cause);
    }

    public AcsCardException(String message, Throwable cause) {
        super(message, cause);
    }
}
