package no.entur.android.nfc.wrapper.tech.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import no.entur.android.nfc.wrapper.tech.IsoDep;

public class LockThreadIsoDepTest {

    @Test
    public void testSingleThread() throws IOException {
        IsoDep delegate = mock(IsoDep.class);

        LockThreadIsoDep isoDep = LockThreadIsoDep.newBuilder().withIsoDep(delegate).build();

        // check that does not blow up for a single thread
        isoDep.connect();
        isoDep.setTimeout(123);
        byte[] c = null;
        isoDep.transceive(c);
        isoDep.close();
    }

    @Test
    public void testMultiple() throws IOException, InterruptedException {
        IsoDep delegate = mock(IsoDep.class);

        LockThreadIsoDep isoDep = LockThreadIsoDep.newBuilder().withIsoDep(delegate).build();

        Thread thread = new Thread("myThread") {
           @Override
            public void run() {
                try {
                    isoDep.connect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread.start();
        thread.join();

        try {
            byte[] c = null;
            isoDep.transceive(c);
            fail();
        } catch(Exception e) {
            // pass

            assertTrue(e.getMessage().contains("myThread"));
        }
    }

}
