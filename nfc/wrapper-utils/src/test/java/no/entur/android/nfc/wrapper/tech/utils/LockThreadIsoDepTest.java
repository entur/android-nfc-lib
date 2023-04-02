package no.entur.android.nfc.wrapper.tech.utils;

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
        isoDep.transceive(null);
        isoDep.close();
    }

    @Test
    public void testMultiple() throws IOException, InterruptedException {
        IsoDep delegate = mock(IsoDep.class);

        LockThreadIsoDep isoDep = LockThreadIsoDep.newBuilder().withIsoDep(delegate).build();

        Thread thread = new Thread() {
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
            isoDep.transceive(null);
            fail();
        } catch(Exception e) {
            // pass
        }
    }

}
