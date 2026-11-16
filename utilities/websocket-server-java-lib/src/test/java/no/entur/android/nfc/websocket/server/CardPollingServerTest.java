package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;

public class CardPollingServerTest {

    private static class TestCardListener implements CardListener {

        private final List<Card> connected = new CopyOnWriteArrayList<>();
        private final List<Card> disconnected = new CopyOnWriteArrayList<>();

        private final CountDownLatch connectedLatch;
        private final CountDownLatch disconnectedLatch;

        private final int failures;

        private TestCardListener(int failures, int connected, int disconnected) {
            this.failures = failures;
            this.connectedLatch = new CountDownLatch(connected);
            this.disconnectedLatch = new CountDownLatch(disconnected);
        }

        @Override
        public void cardConnected(Card card) throws CardException {
            connected.add(card);
            connectedLatch.countDown();
            if(connected.size() <= failures) {
                throw new RuntimeException("Unable to handle card");
            }
        }

        @Override
        public void cardDisconnected(Card card) {
            disconnected.add(card);
            disconnectedLatch.countDown();
        }
    }

    @Test
    public void testCardIsDisconnectedWhenRemoved() throws Exception {
        FakeCardTerminal terminal = new FakeCardTerminal("Reader");

        TestCardListener listener = new TestCardListener(0, 1, 1);

        CardPollingServer server = new CardPollingServer(terminal);
        server.setListener(listener);
        server.start();
        try {
            terminal.setCardPresent(true);
            assertTrue(listener.connectedLatch.await(5, TimeUnit.SECONDS));

            terminal.setCardPresent(false);
            assertTrue(listener.disconnectedLatch.await(5, TimeUnit.SECONDS));

            waitForDisconnected(terminal.getConnected().get(0));
        } finally {
            server.stop();
        }
    }

    @Test
    public void testProblemWithCardDoesNotStopPolling() throws Exception {
        FakeCardTerminal terminal = new FakeCardTerminal("Reader");

        // first card fails, second card works
        TestCardListener listener = new TestCardListener(1, 2, 1);

        CardPollingServer server = new CardPollingServer(terminal);
        server.setListener(listener);
        server.start();
        try {
            terminal.setCardPresent(true);
            Thread.sleep(300);

            // failed card is connected only once, i.e. not retried while still present
            assertEquals(1, listener.connected.size());
            // client was never told about the card, so no disconnect either
            terminal.setCardPresent(false);
            waitForDisconnected(terminal.getConnected().get(0));
            assertTrue(listener.disconnected.isEmpty());

            terminal.setCardPresent(true);
            assertTrue(listener.connectedLatch.await(5, TimeUnit.SECONDS));

            terminal.setCardPresent(false);
            assertTrue(listener.disconnectedLatch.await(5, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
    }

    @Test
    public void testRestartAfterReaderProblem() throws Exception {
        FakeCardTerminal failing = new FakeCardTerminal("Reader") {
            private volatile boolean fail = true;

            @Override
            public boolean isCardPresent() throws CardException {
                if(fail) {
                    fail = false;
                    throw new CardException("Reader gone");
                }
                return super.isCardPresent();
            }
        };

        TestCardListener listener = new TestCardListener(0, 1, 0);

        CardPollingServer server = new CardPollingServer(failing);
        server.setListener(listener);
        server.start();
        try {
            // thread stopped because of the reader problem
            Thread.sleep(200);

            failing.setCardPresent(true);
            server.start();

            assertTrue(listener.connectedLatch.await(5, TimeUnit.SECONDS));
        } finally {
            server.stop();
        }
    }

    private static void waitForDisconnected(FakeCard card) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while(!card.isDisconnected() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(card.isDisconnected());
    }
}
