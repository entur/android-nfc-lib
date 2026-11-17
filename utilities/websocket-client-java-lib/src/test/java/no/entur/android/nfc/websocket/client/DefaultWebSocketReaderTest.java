package no.entur.android.nfc.websocket.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardApduRequestMessage;
import okhttp3.OkHttpClient;

public class DefaultWebSocketReaderTest {

    @Test
    public void testFailureWakesWaitingRequest() throws Exception {
        RequestResponseMessages messages = new RequestResponseMessages((m) -> {}, (m) -> {});

        CountDownLatch closed = new CountDownLatch(1);
        DefaultWebSocketReader reader = new DefaultWebSocketReader(messages, new OkHttpClient(), messages);
        reader.setListener(closed::countDown);

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<NfcMessage> result = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            result.set(messages.sendAndWaitForResponse(new CardApduRequestMessage(new byte[]{0x00}), 60000));
            done.countDown();
        });
        thread.start();

        Thread.sleep(100);

        // i.e. connection lost; there will be no responses
        reader.onFailure(null, new IOException("Connection reset"), null);

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(result.get());
        assertTrue(closed.await(1, TimeUnit.SECONDS));

        // later requests do not wait either
        assertNull(messages.sendAndWaitForResponse(new CardApduRequestMessage(new byte[]{0x00}), 60000));
    }
}
