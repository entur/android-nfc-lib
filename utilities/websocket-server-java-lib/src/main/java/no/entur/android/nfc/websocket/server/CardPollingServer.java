package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public class CardPollingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardPollingServer.class);

    private Thread thread;

    private boolean closed = false;

    private final CardTerminal cardTerminal;

    private CardListener listener;

    public CardPollingServer(CardTerminal cardTerminal) {
        this.cardTerminal = cardTerminal;
    }

    public void setListener(CardListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        LOGGER.info("Start polling for card");
        while(!closed) {
            try {
                if(cardTerminal.waitForCardPresent(0)) {
                    Card card = cardTerminal.connect("*");

                    CardListener listener = this.listener; // defensive copy
                    listener.cardConnected(card);
                    try {
                        cardTerminal.waitForCardAbsent(0);
                    } finally {
                        listener.cardDisconnected(card);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Problem running", e);
            }
        }
        LOGGER.info("Stop polling for card");
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        closed = true;

        if(thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

}
