package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.TerminalFactorySpi;

public class CardTerminalsPollingServerTest {

    public static class FakeTerminalFactorySpi extends TerminalFactorySpi {

        private final CardTerminals terminals;

        public FakeTerminalFactorySpi(Object parameter) {
            this.terminals = (CardTerminals) parameter;
        }

        @Override
        protected CardTerminals engineTerminals() {
            return terminals;
        }
    }

    private static class FakeCardTerminals extends CardTerminals {

        private final AtomicInteger listCount = new AtomicInteger();

        private final Supplier<List<CardTerminal>> terminals;

        private FakeCardTerminals(Supplier<List<CardTerminal>> terminals) {
            this.terminals = terminals;
        }

        @Override
        public List<CardTerminal> list(State state) throws CardException {
            listCount.incrementAndGet();
            return terminals.get();
        }

        @Override
        public boolean waitForChange(long timeout) {
            return false;
        }
    }

    private static class CountingListener implements CardTerminalsPollingListener {

        private final List<String> connected = new CopyOnWriteArrayList<>();
        private final List<String> disconnected = new CopyOnWriteArrayList<>();

        @Override
        public void connected(CardTerminal cardTerminal) throws CardException {
            connected.add(cardTerminal.getName());

            if(cardTerminal.getName().contains("Unsupported")) {
                // like ExtendedCardTerminalFactory
                throw new RuntimeException("Unsupported reader " + cardTerminal.getName());
            }
        }

        @Override
        public void disconnected(CardTerminal cardTerminal) {
            disconnected.add(cardTerminal.getName());
        }
    }

    private static TerminalFactory createFactory(CardTerminals terminals) throws Exception {
        Provider provider = new Provider("FakePCSC", "1.0", "Fake PC/SC") {
            {
                put("TerminalFactory.Fake", FakeTerminalFactorySpi.class.getName());
            }
        };
        return TerminalFactory.getInstance("Fake", terminals, provider);
    }

    @Test
    public void testUnsupportedReaderIsNotRetried() throws Exception {
        // new instances on every list, like jnasmartcardio
        FakeCardTerminals terminals = new FakeCardTerminals(() -> {
            List<CardTerminal> list = new ArrayList<>();
            list.add(new FakeCardTerminal("Unsupported reader"));
            list.add(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC"));
            return list;
        });

        CountingListener listener = new CountingListener();

        CardTerminalsPollingServer server = new CardTerminalsPollingServer(createFactory(terminals), listener);
        server.setDelay(20);
        server.start();
        try {
            Thread.sleep(500);
        } finally {
            server.stop();
        }

        // without errors, i.e. about 25 iterations, and not a busy loop
        assertTrue(terminals.listCount.get() > 1);
        assertTrue(terminals.listCount.get() < 100, "Got " + terminals.listCount.get());

        // each reader is connected once
        assertEquals(2, listener.connected.size(), listener.connected.toString());
        assertTrue(listener.disconnected.isEmpty());
    }

    @Test
    public void testBackOffWhenListingFails() throws Exception {
        FakeCardTerminals terminals = new FakeCardTerminals(() -> {
            // i.e. PC/SC not running
            throw new IllegalStateException("No PC/SC");
        });

        CountingListener listener = new CountingListener();

        CardTerminalsPollingServer server = new CardTerminalsPollingServer(createFactory(terminals), listener);
        server.setDelay(20);
        server.start();
        try {
            Thread.sleep(500);
        } finally {
            server.stop();
        }

        assertTrue(terminals.listCount.get() > 1);
        assertTrue(terminals.listCount.get() < 100, "Got " + terminals.listCount.get());
    }

    @Test
    public void testRemovedReader() throws Exception {
        List<String> names = new CopyOnWriteArrayList<>();
        names.add("ACS ACR1252 Dual Reader PICC");

        FakeCardTerminals terminals = new FakeCardTerminals(() -> {
            List<CardTerminal> list = new ArrayList<>();
            for (String name : names) {
                list.add(new FakeCardTerminal(name));
            }
            return list;
        });

        CountingListener listener = new CountingListener();

        CardTerminalsPollingServer server = new CardTerminalsPollingServer(createFactory(terminals), listener);
        server.setDelay(20);
        server.start();
        try {
            Thread.sleep(200);
            names.clear();
            Thread.sleep(200);
        } finally {
            server.stop();
        }

        assertEquals(1, listener.connected.size());
        assertEquals(1, listener.disconnected.size());
    }
}
