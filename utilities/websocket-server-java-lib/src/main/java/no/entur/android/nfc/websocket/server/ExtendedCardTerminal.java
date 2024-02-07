package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public abstract class ExtendedCardTerminal extends CardTerminal {

    protected final CardTerminal delegate;

    public ExtendedCardTerminal(CardTerminal delegate) {
        this.delegate = delegate;
    }


    public abstract void startPolling() throws CardException;

    public abstract void stopPolling() throws CardException;

    public CardTerminal getDelegate() {
        return delegate;
    }
}
