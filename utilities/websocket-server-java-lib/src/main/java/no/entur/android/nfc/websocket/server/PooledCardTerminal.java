package no.entur.android.nfc.websocket.server;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class PooledCardTerminal {

    private final ExtendedCardTerminal cardTerminal;

    private volatile boolean borrowed = false;

    private final CardPollingServer cardPollingServer;

    private boolean closed;

    public PooledCardTerminal(ExtendedCardTerminal cardTerminal) {
        this.cardTerminal = cardTerminal;
        this.cardPollingServer = new CardPollingServer(cardTerminal);
    }

    public CardTerminal getCardTerminal() {
        return cardTerminal.getDelegate();
    }

    public void setListener(CardListener listener) {
        this.cardPollingServer.setListener(listener);
    }

    public void startPolling() throws CardException {
        cardTerminal.startPolling();
        cardPollingServer.start();
    }

    public void stopPolling() throws CardException {
        cardPollingServer.stop();
        cardTerminal.stopPolling();
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public void borrow() {
        this.borrowed = true;
    }

    public void unborrow() {
        this.borrowed = false;

        cardPollingServer.setListener(null);
    }

    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
