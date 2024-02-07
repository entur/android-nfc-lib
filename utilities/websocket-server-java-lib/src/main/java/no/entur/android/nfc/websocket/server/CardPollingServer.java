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

        main:
        while(!closed) {
            try {
                if(cardTerminal.waitForCardPresent(100)) {
                    Card card = cardTerminal.connect("*");

                    CardListener listener = this.listener; // defensive copy
                    listener.cardConnected(card);

                    while(!closed) {
                        try {
                            Thread.sleep(100);
                            if(!cardTerminal.isCardPresent()) {
                                listener.cardDisconnected(card);
                            }
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            if(!cardTerminal.isCardPresent()) {
                                listener.cardDisconnected(card);
                            }
                            break main;
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("Problem running", e);

                break;
            }
            LOGGER.info("Polling for card");

            Thread.yield();
        }
        LOGGER.info("Stop polling for card");
    }

    public void start() {
        closed = false;

        LOGGER.info("Start background thread");

        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        LOGGER.info("Stop background thread");

        closed = true;

        if(thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

}
