package no.entur.android.nfc.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CardTerminalsPollingPool implements CardTerminalsPollingListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(CardTerminalsPollingPool.class);

    private List<PooledCardTerminal> current = new ArrayList<>();

    private final ExtendedCardTerminalFactory extendedCardTerminalFactory;

    public CardTerminalsPollingPool(ExtendedCardTerminalFactory extendedCardTerminalFactory) {
        this.extendedCardTerminalFactory = extendedCardTerminalFactory;
    }

    @Override
    public void connected(CardTerminal cardTerminal) throws CardException {
        LOGGER.info("Connect reader " + cardTerminal.getName());
        ExtendedCardTerminal extendedCardTerminal = extendedCardTerminalFactory.create(cardTerminal);
        synchronized (current) {
            current.add(new PooledCardTerminal(extendedCardTerminal));
            LOGGER.info("Connected reader " + cardTerminal.getName() + ", now have " + current.size() + " readers");
        }
    }

    @Override
    public void disconnected(CardTerminal cardTerminal) {
        synchronized (current) {
            for (int i = 0; i < current.size(); i++) {
                PooledCardTerminal pooledCardTerminal = current.get(i);
                if (cardTerminal == pooledCardTerminal.getCardTerminal().getDelegate()) {
                    current.remove(i);

                    pooledCardTerminal.close();
                    break;
                }
            }
        }
    }

    public PooledCardTerminal borrow(List<String> tags) {
        synchronized (current) {
            for(PooledCardTerminal terminal : current) {
                if(!terminal.isBorrowed() && terminal.matches(tags)) {
                    LOGGER.info("Borrowing reader from pool of " + current.size() + " for " + tags);
                    terminal.borrow();
                    return terminal;
                }
            }
            LOGGER.info("Unable to borrow reader from pool of " + current.size() + " for " + tags);
        }

        return null;
    }

    public void unborrow(PooledCardTerminal t) {
        synchronized (current) {
            t.unborrow();
        }
    }

}
