package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CardPollingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardPollingServer.class);

    // the active polling thread, if any. A stopped thread might still be finishing (i.e. blocked in a callback),
    // so each thread checks whether it is still the active one, rather than sharing a single closed flag.
    private volatile Thread thread;

    // the last stopped thread, which the next thread must wait for before connecting to a card,
    // so that its cleanup (card disconnect) does not interfere with the next thread.
    private Thread previous;

    private final CardTerminal cardTerminal;

    private volatile CardListener listener;

    public CardPollingServer(CardTerminal cardTerminal) {
        this.cardTerminal = cardTerminal;
    }

    public void setListener(CardListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        poll(null);
    }

    private boolean isActive() {
        return thread == Thread.currentThread();
    }

    private void poll(Thread previous) {
        if(previous != null) {
            // wait outside of any monitor, as the previous thread might need it.
            // Wait even if stopped, so that a chain of quick restarts waits for all previous threads.
            awaitTermination(previous);
        }
        LOGGER.info("Start polling for card");

        long timestamp = System.currentTimeMillis();

        long counter = 0;

        main:
        while(isActive()) {
            try {
                if(cardTerminal.isCardPresent()) {
                    LOGGER.info("Card is present");
                    Card card;
                    try {
                        card = cardTerminal.connect("*");
                    } catch (CardException e) {
                        // i.e. the card was removed after the presence check. A problem with a single card must not
                        // stop polling, but do not retry while the same card is present.
                        LOGGER.warn("Unable to connect to card, wait for it to be removed", e);

                        waitForCardAbsent();

                        continue;
                    }
                    try {
                        CardListener listener = this.listener; // defensive copy

                        // a problem with a single card must not stop polling
                        boolean connected = false;
                        try {
                            listener.cardConnected(card);

                            connected = true;
                        } catch (Exception e) {
                            LOGGER.warn("Problem handling card, wait for it to be removed", e);
                        }

                        while(isActive()) {
                            try {
                                Thread.sleep(100);
                                if(!cardTerminal.isCardPresent()) {
                                    if(connected) {
                                        listener.cardDisconnected(card);
                                    }

                                    break;
                                }
                            } catch (InterruptedException e) {
                                Thread.interrupted();
                                if(!cardTerminal.isCardPresent()) {
                                    if(connected) {
                                        listener.cardDisconnected(card);
                                    }

                                    break;
                                }
                                break main;
                            }
                        }
                    } finally {
                        disconnect(card);
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

                // mark as stopped so that polling can be started again
                synchronized (this) {
                    if(thread == Thread.currentThread()) {
                        thread = null;
                        this.previous = Thread.currentThread();
                    }
                }

                break;
            }
        }
        LOGGER.info("Stop polling for card");
    }

    /**
     * Wait for the card to be removed, or polling to be stopped. Reader problems are thrown,
     * so that they stop polling like elsewhere.
     */

    private void waitForCardAbsent() throws CardException {
        while(isActive() && cardTerminal.isCardPresent()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // stopped (checked by isActive), or a spurious interrupt
                Thread.interrupted();
            }
        }
    }

    private static void awaitTermination(Thread previous) {
        boolean interrupted = false;
        while(previous.isAlive()) {
            try {
                previous.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if(interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void disconnect(Card card) {
        try {
            card.disconnect(false);
        } catch (Exception e) {
            // i.e. already disconnected when polling was ended
            LOGGER.debug("Problem disconnecting card", e);
        }
    }

    public synchronized void start() {
        if(thread == null) {
            LOGGER.info("Start background thread");

            Thread previous = this.previous;
            this.previous = null;

            Thread next = new Thread(() -> poll(previous));
            thread = next;
            next.start();
        } else {
            LOGGER.info("Background thread already started");
        }
    }

    public synchronized void stop() {
        LOGGER.info("Stop background thread");

        Thread current = thread;
        if(current != null) {
            thread = null;
            previous = current;

            current.interrupt();
        }
    }

}
