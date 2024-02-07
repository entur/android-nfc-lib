package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class CardTerminalsPollingServer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardTerminalsPollingServer.class);

    private long delay = 1000;

    private Thread thread;

    private boolean closed = false;

    private List<CardTerminal> current = new ArrayList<>();

    private List<CardTerminal> ignored = new ArrayList<>();

    private final TerminalFactory factory;

    private final CardTerminalsPollingListener listener;

    private CardTerminalsFilter cardTerminalsFilter = new NoopCardTerminalsFilter();

    public CardTerminalsPollingServer(TerminalFactory factory, CardTerminalsPollingListener listener) {
        this.factory = factory;
        this.listener = listener;
    }

    public void setCardTerminalsFilter(CardTerminalsFilter cardTerminalsFilter) {
        this.cardTerminalsFilter = cardTerminalsFilter;
    }

    @Override
    public void run() {
        LOGGER.info("Start polling for readers");
        while(!closed) {
            try {
                List<CardTerminal> added = new ArrayList<>();
                List<CardTerminal> removed = new ArrayList<>();

                List<CardTerminal> accept = new ArrayList<>();
                List<CardTerminal> ignore = new ArrayList<>();

                for (CardTerminal cardTerminal : factory.terminals().list()) {
                    if(cardTerminalsFilter.accept(cardTerminal)) {
                        accept.add(cardTerminal);
                    } else {
                        ignore.add(cardTerminal);
                    }
                }

                ignored:
                for(CardTerminal candidate : ignore) {
                    for(int i = 0; i < ignored.size(); i++) {
                        CardTerminal c = ignored.get(i);
                        if(candidate == c || candidate.getName().equals(c.getName())) {
                            continue ignored;
                        }
                    }
                    LOGGER.info("Ignore reader " + candidate.getName());
                }

                added:
                for(CardTerminal candidate : accept) {
                    for(int i = 0; i < current.size(); i++) {
                        CardTerminal c = current.get(i);
                        if(candidate == c || candidate.getName().equals(c.getName())) {
                            continue added;
                        }
                    }
                    added.add(candidate);
                }

                removed:
                for(int i = 0; i < current.size(); i++) {
                    CardTerminal c = current.get(i);
                    for(CardTerminal candidate : accept) {
                        if(candidate == c || candidate.getName().equals(c.getName())) {
                            continue removed;
                        }
                    }
                    removed.add(c);
                }

                if(!added.isEmpty()) {
                    LOGGER.info("Detected " + added.size() + " new readers");

                    for(int i = 0; i < added.size(); i++) {
                        CardTerminal cardTerminal = added.get(i);

                        LOGGER.info("Add new reader " + cardTerminal);

                        try {
                            listener.connected(cardTerminal);
                        } catch(CardException e) {
                            LOGGER.info("Unable to connect " + cardTerminal.getName() +", adding to ignore", e);

                            ignore.add(cardTerminal);

                            added.remove(i);
                            i--;
                        }
                    }
                }

                if(!removed.isEmpty()) {
                    LOGGER.info("Detected " + removed.size() + " removed readers");

                    for (CardTerminal cardTerminal : removed) {
                        listener.disconnected(cardTerminal);
                    }
                }

                LOGGER.info("Currently have " + accept.size() + " readers");

                this.current = accept;
                this.ignored = ignore;

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

    // XXX seems now all use the same code
    public static int CONTROL_CODE() {

        String osName = System.getProperty( "os.name" ).toLowerCase();
        if ( osName.indexOf( "windows" ) > -1 ) {
            /* Value used by both MS' CCID driver and SpringCard's CCID driver */
            return (0x31 << 16 | 3500 << 2);
        }
        else {
            /* Value used by PCSC-Lite */
            return 0x42000000 + 1;
        }

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
