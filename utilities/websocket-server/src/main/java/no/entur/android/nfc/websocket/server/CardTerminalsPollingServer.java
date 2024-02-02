package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class CardTerminalsPollingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardTerminalsPollingServer.class);

    private long delay = 1000;

    private Thread thread;

    private boolean closed = false;

    private List<CardTerminal> current = new ArrayList<>();

    private final TerminalFactory factory;

    private final CardTerminalsPollingListener listener;

    public CardTerminalsPollingServer(TerminalFactory factory, CardTerminalsPollingListener listener) {
        this.factory = factory;
        this.listener = listener;
    }

    @Override
    public void run() {
        LOGGER.info("Start polling for readers");
        while(!closed) {
            try {
                List<CardTerminal> added = new ArrayList<>();
                List<CardTerminal> removed = new ArrayList<>();

                List<CardTerminal> list = factory.terminals().list();

                added:
                for(CardTerminal candidate : list) {
                    for(int i = 0; i < current.size(); i++) {
                        CardTerminal c = current.get(i);
                        if(candidate == c) {
                            continue added;
                        }
                    }
                    added.add(candidate);
                }

                removed:
                for(int i = 0; i < current.size(); i++) {
                    CardTerminal c = current.get(i);
                    for(CardTerminal candidate : list) {
                        if(candidate == c) {
                            continue removed;
                        }
                    }
                    removed.add(c);
                }

                if(!added.isEmpty()) {
                    LOGGER.info("Found " + added.size() + " new readers");

                    for (CardTerminal cardTerminal : added) {
                        listener.connected(cardTerminal);
                    }
                }

                if(!removed.isEmpty()) {
                    LOGGER.info("Found " + added.size() + " removed readers");

                    for (CardTerminal cardTerminal : removed) {
                        listener.disconnected(cardTerminal);
                    }
                }

                LOGGER.info("Currently have " + list.size() + " readers");

                this.current = list;

                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.interrupted();

                break;
                // ignore
            } catch (Exception e) {
                LOGGER.error("Problem running", e);
            }
        }
        LOGGER.info("Stop polling for readers");
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        closed = true;

        if(thread != null) {
            thread.interrupt();
        }
    }



}
