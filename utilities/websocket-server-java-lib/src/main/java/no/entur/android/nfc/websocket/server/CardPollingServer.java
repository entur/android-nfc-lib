package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

public class CardPollingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardPollingServer.class);

    private Thread thread;

    private boolean closed = true;

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

        long timestamp = System.currentTimeMillis();

        long counter = 0;

        main:
        while(!closed) {
            try {
                if(cardTerminal.isCardPresent()) {
                    LOGGER.info("Card is present");
                    Card card = cardTerminal.connect("*");

                    CardListener listener = this.listener; // defensive copy
                    listener.cardConnected(card);

                    while(!closed) {
                        try {
                            Thread.sleep(100);
                            if(!cardTerminal.isCardPresent()) {
                                listener.cardDisconnected(card);

                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            if(!cardTerminal.isCardPresent()) {
                                listener.cardDisconnected(card);

                                break;
                            }
                            break main;
                        }
                    }
                }
                Thread.sleep(10);

                long duration = (System.currentTimeMillis() - timestamp) / 1000;
                if(duration > counter) {
                    LOGGER.info("Polling for card: " + duration + "s");
                    counter = duration;
                }

            } catch (Exception e) {
                LOGGER.error("Problem running", e);

                break;
            }
        }
        LOGGER.info("Stop polling for card");
    }

    public void start() {
        if(closed) {
            closed = false;

            LOGGER.info("Start background thread");

            thread = new Thread(this);
            thread.start();
        } else {
            LOGGER.info("Background thread already started");
        }
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
