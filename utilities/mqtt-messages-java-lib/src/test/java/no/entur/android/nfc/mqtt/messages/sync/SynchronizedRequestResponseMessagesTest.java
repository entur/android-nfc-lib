package no.entur.android.nfc.mqtt.messages.sync;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SynchronizedRequestResponseMessagesTest {

    private static final String TOPIC = "response/topic";

    /**
     * Request which only accepts responses with the same number.
     */

    private static class NumberedRequest extends DefaultSynchronizedRequestMessageRequest<String, Integer> {

        public NumberedRequest(int number) {
            super(TOPIC, number, "request/topic");
        }

        @Override
        public boolean isResponse(SynchronizedResponseMessage<String> response) {
            return ((NumberedResponse) response).number == getPayload();
        }
    }

    private static class NumberedResponse extends DefaultSynchronizedResponseMessage<String> {

        private final int number;

        public NumberedResponse(int number) {
            super(TOPIC);
            this.number = number;
        }
    }

    @Test
    public void testResponse() throws IOException {
        SynchronizedRequestResponseMessages<String> messages = new SynchronizedRequestResponseMessages<>();
        NumberedResponse response = new NumberedResponse(1);
        messages.setRequestMessageListener((request, listener) -> listener.onResponseMessage(response));

        assertSame(response, messages.sendAndWaitForResponse(new NumberedRequest(1), 1000));
    }

    @Test
    public void testLateResponseIsNotTakenAsResponseToNextRequest() throws IOException {
        SynchronizedRequestResponseMessages<String> messages = new SynchronizedRequestResponseMessages<>();

        // first request times out
        messages.setRequestMessageListener((request, listener) -> {
        });
        assertThrows(SynchronizedResponseMessageTimeoutException.class, () -> messages.sendAndWaitForResponse(new NumberedRequest(1), 50));

        // the late response to the first request arrives while the second request waits, then the response to the second request
        NumberedResponse response = new NumberedResponse(2);
        messages.setRequestMessageListener((request, listener) -> {
            listener.onResponseMessage(new NumberedResponse(1));
            listener.onResponseMessage(response);
        });

        assertSame(response, messages.sendAndWaitForResponse(new NumberedRequest(2), 1000));
    }

    @Test
    public void testResponseWithoutRequestIsIgnored() throws IOException {
        SynchronizedRequestResponseMessages<String> messages = new SynchronizedRequestResponseMessages<>();

        // i.e. after a timeout
        messages.onResponseMessage(new NumberedResponse(1));

        NumberedResponse response = new NumberedResponse(2);
        messages.setRequestMessageListener((request, listener) -> listener.onResponseMessage(response));

        assertSame(response, messages.sendAndWaitForResponse(new NumberedRequest(2), 1000));
    }

    @Test
    public void testTimeout() {
        SynchronizedRequestResponseMessages<String> messages = new SynchronizedRequestResponseMessages<>();
        messages.setRequestMessageListener((request, listener) -> {
        });

        long time = System.currentTimeMillis();
        assertThrows(SynchronizedResponseMessageTimeoutException.class, () -> messages.sendAndWaitForResponse(new NumberedRequest(1), 100));
        assertTrue(System.currentTimeMillis() - time >= 100);
    }

    @Test
    public void testCloseStopsWaiting() throws Exception {
        SynchronizedRequestResponseMessages<String> messages = new SynchronizedRequestResponseMessages<>();
        CompletableFuture<Void> sent = new CompletableFuture<>();
        messages.setRequestMessageListener((request, listener) -> sent.complete(null));

        CompletableFuture<Exception> result = CompletableFuture.supplyAsync(() -> {
            try {
                messages.sendAndWaitForResponse(new NumberedRequest(1), 10000);
                return null;
            } catch (Exception e) {
                return e;
            }
        });
        sent.get(1, TimeUnit.SECONDS);
        Thread.sleep(50);

        messages.close();

        Exception exception = result.get(1, TimeUnit.SECONDS);
        assertTrue(exception instanceof IOException);
        assertTrue(!(exception instanceof SynchronizedResponseMessageTimeoutException));
    }

    @Test
    public void testSpuriousWakeup() throws Exception {
        SynchronizedRequestResponseMessageLock<String> lock = new SynchronizedRequestResponseMessageLock<>(new NumberedRequest(1));

        CompletableFuture<Exception> result = CompletableFuture.supplyAsync(() -> {
            try {
                lock.waitForMessage(300);
                return null;
            } catch (Exception e) {
                return e;
            }
        });
        Thread.sleep(50);
        synchronized (lock) {
            lock.notifyAll();
        }

        // still waiting after the wakeup, then times out
        Thread.sleep(50);
        assertTrue(!result.isDone());
        assertTrue(result.get(1, TimeUnit.SECONDS) instanceof SynchronizedResponseMessageTimeoutException);
    }
}
