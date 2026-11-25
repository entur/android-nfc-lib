package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

public class CardTerminalsPollingPoolTest {

    private static class TestExtendedCardTerminal extends ExtendedCardTerminal {

        public TestExtendedCardTerminal(CardTerminal delegate) {
            super(delegate);
            setTags(Collections.emptyList());
        }

        @Override
        public void startPolling() {
        }

        @Override
        public void stopPolling() {
        }

        @Override
        public List<String> identifyTechnologies(Card card, CardChannel channel) {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Card connect(String protocol) throws CardException {
            return delegate.connect(protocol);
        }

        @Override
        public boolean isCardPresent() throws CardException {
            return delegate.isCardPresent();
        }

        @Override
        public boolean waitForCardPresent(long timeout) throws CardException {
            return delegate.waitForCardPresent(timeout);
        }

        @Override
        public boolean waitForCardAbsent(long timeout) throws CardException {
            return delegate.waitForCardAbsent(timeout);
        }
    }

    private static class TestExtendedCardTerminalFactory extends ExtendedCardTerminalFactory {

        public TestExtendedCardTerminalFactory() {
            super(new DefaultCardTerminalMetadataEnricher());
        }

        @Override
        public ExtendedCardTerminal create(CardTerminal cardTerminal) {
            return new TestExtendedCardTerminal(cardTerminal);
        }
    }

    @Test
    public void testDisconnectedWithOtherInstanceOfSameReader() throws CardException {
        CardTerminalsPollingPool pool = new CardTerminalsPollingPool(new TestExtendedCardTerminalFactory());

        pool.connected(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC"));

        PooledCardTerminal borrowed = pool.borrow(Collections.emptyList());
        assertNotNull(borrowed);
        pool.unborrow(borrowed);

        // the terminal list returns new instances
        pool.disconnected(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC"));

        assertTrue(borrowed.isClosed());
        assertNull(pool.borrow(Collections.emptyList()));
    }

    @Test
    public void testDisconnectedOtherReader() throws CardException {
        CardTerminalsPollingPool pool = new CardTerminalsPollingPool(new TestExtendedCardTerminalFactory());

        pool.connected(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC"));

        pool.disconnected(new FakeCardTerminal("ACS ACR1252 Dual Reader PICC 01"));

        assertNotNull(pool.borrow(Collections.emptyList()));
    }
}
