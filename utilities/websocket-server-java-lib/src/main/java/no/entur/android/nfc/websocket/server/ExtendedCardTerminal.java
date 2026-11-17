package no.entur.android.nfc.websocket.server;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public abstract class ExtendedCardTerminal extends CardTerminal {

    protected final CardTerminal delegate;
    protected List<String> tags;

    public ExtendedCardTerminal(CardTerminal delegate) {
        this.delegate = delegate;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public abstract void startPolling() throws CardException;

    public abstract void stopPolling() throws CardException;

    public CardTerminal getDelegate() {
        return delegate;
    }

    public abstract List<String> identifyTechnologies(Card card, CardChannel channel);

    /**
     * Exchange native ISO 14443-A (layer 3) commands with the card in the field, outside of the PC/SC card connection.
     * The card might be re-activated, so any connection to the card must be reestablished afterwards.
     *
     * @param commands native commands
     * @return a response per command (null if no response, i.e. not supported by the card), or null if not supported by the reader
     * @throws CardException if the exchange failed
     */

    public List<byte[]> transceiveTransparent(List<byte[]> commands) throws CardException {
        return null;
    }

    /**
     * Whether the reader appends a status word 90 00 to card responses of a single byte (i.e. native DESFire responses).
     *
     * @return true if the status word is appended
     */

    public boolean isStatusWordAppendedToSingleByteResponses() {
        return false;
    }
}
