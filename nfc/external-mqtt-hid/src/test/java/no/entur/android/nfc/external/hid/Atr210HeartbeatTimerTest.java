package no.entur.android.nfc.external.hid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import java.util.concurrent.Executors;

public class Atr210HeartbeatTimerTest {

    @Test
    public void testCheckAgainWhileThereAreReaders() throws Exception {
        Atr210MqttHandler handler = mock(Atr210MqttHandler.class);
        // readers, readers, then no readers
        when(handler.verifyHeartbeats()).thenReturn(true, true, false);

        Atr210HeartbeatTimer timer = new Atr210HeartbeatTimer(10, Executors.newSingleThreadScheduledExecutor(), handler);
        try {
            timer.schedule();

            verify(handler, timeout(2000).times(3)).verifyHeartbeats();

            // no more checks when there are no readers
            Thread.sleep(100);
            verify(handler, times(3)).verifyHeartbeats();
        } finally {
            timer.close();
        }
    }

    @Test
    public void testCheckAgainAfterException() throws Exception {
        Atr210MqttHandler handler = mock(Atr210MqttHandler.class);
        when(handler.verifyHeartbeats()).thenThrow(new RuntimeException()).thenReturn(false);

        Atr210HeartbeatTimer timer = new Atr210HeartbeatTimer(10, Executors.newSingleThreadScheduledExecutor(), handler);
        try {
            timer.schedule();

            verify(handler, timeout(2000).times(2)).verifyHeartbeats();
        } finally {
            timer.close();
        }
    }
}
