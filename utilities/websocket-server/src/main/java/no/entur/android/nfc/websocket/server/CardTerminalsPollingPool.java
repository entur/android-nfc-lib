package no.entur.android.nfc.websocket.server;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardTerminal;

public class CardTerminalsPollingPool implements CardTerminalsPollingListener {

    private List<PooledCardTerminal> current = new ArrayList<>();

    @Override
    public void connected(CardTerminal cardTerminal) {
        synchronized (current) {
            current.add(new PooledCardTerminal(cardTerminal));
        }
    }

    @Override
    public void disconnected(CardTerminal cardTerminal) {
        synchronized (current) {
            for (int i = 0; i < current.size(); i++) {
                PooledCardTerminal pooledCardTerminal = current.get(i);
                if (cardTerminal == pooledCardTerminal.getCardTerminal()) {
                    current.remove(i);

                    pooledCardTerminal.close();
                    break;
                }
            }
        }
    }

    public PooledCardTerminal borrow() {
        synchronized (current) {
            for(PooledCardTerminal terminal : current) {
                if(!terminal.isBorrowed()) {
                    terminal.borrow();
                    return terminal;
                }
            }
        }
        return null;
    }

    public void unborrow(PooledCardTerminal t) {
        synchronized (current) {
            t.unborrow();
        }
    }

}
