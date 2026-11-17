package no.entur.android.nfc.websocket.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    public void testConnectProblemDoesNotStopPolling() throws Exception {
        // i.e. the card is removed between the presence check and connect
        AtomicInteger connects = new AtomicInteger();
        FakeCardTerminal terminal = new FakeCardTerminal("Reader") {
            @Override
            public Card connect(String protocol) throws CardException {
                if(connects.incrementAndGet() == 1) {
                    throw new CardException("Card removed");
                }
                return super.connect(protocol);
            }
        };

        TestCardListener listener = new TestCardListener(0, 1, 1);

        CardPollingServer server = new CardPollingServer(terminal);
        server.setListener(listener);
        server.start();
        try {
            terminal.setCardPresent(true);
            Thread.sleep(300);

            // not retried while the card is still present
            assertEquals(1, connects.get());
            assertTrue(listener.connected.isEmpty());

            terminal.setCardPresent(false);
            Thread.sleep(200);

            // still polling
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

    @Test
    public void testRestartWhileCardCallbackIsBlocked() throws Exception {
        // like the PC/SC terminal, return the same card while it is connected
        AtomicReference<FakeCard> current = new AtomicReference<>();
        FakeCardTerminal terminal = new FakeCardTerminal("Reader", () -> current.updateAndGet(c -> c == null || c.isDisconnected() ? new FakeCard() : c));

        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Card> connected = new CopyOnWriteArrayList<>();
        CountDownLatch secondConnected = new CountDownLatch(2);

        CardListener listener = new CardListener() {
            @Override
            public void cardConnected(Card card) {
                connected.add(card);
                secondConnected.countDown();
                if(connected.size() == 1) {
                    blocked.countDown();
                    // block, ignoring interrupts, like a native call or blocking send
                    boolean interrupted = false;
                    while(release.getCount() > 0) {
                        try {
                            release.await();
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                    if(interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            @Override
            public void cardDisconnected(Card card) {
            }
        };

        CardPollingServer server = new CardPollingServer(terminal);
        server.setListener(listener);
        server.start();
        try {
            terminal.setCardPresent(true);
            assertTrue(blocked.await(5, TimeUnit.SECONDS));

            // restart while the first thread is blocked in the callback, card still present
            server.stop();
            server.start();

            Thread.sleep(300);
            // the new thread must not connect before the previous thread is done
            assertEquals(1, connected.size());

            release.countDown();

            assertTrue(secondConnected.await(5, TimeUnit.SECONDS));

            FakeCard first = (FakeCard) connected.get(0);
            FakeCard second = (FakeCard) connected.get(1);

            // the previous thread disconnected its card before the new thread connected
            assertTrue(first.isDisconnected());
            assertNotSame(first, second);

            // the previous thread does not keep polling or disconnect the new card
            Thread.sleep(300);
            assertFalse(second.isDisconnected());
            assertEquals(2, connected.size());
        } finally {
            release.countDown();
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
