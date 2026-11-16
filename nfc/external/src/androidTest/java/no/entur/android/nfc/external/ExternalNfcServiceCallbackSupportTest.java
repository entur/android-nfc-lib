package no.entur.android.nfc.external;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class ExternalNfcServiceCallbackSupportTest {

    private final AtomicInteger started = new AtomicInteger();
    private final AtomicInteger stopped = new AtomicInteger();

    private RecordingService context;
    private ExternalNfcServiceCallbackSupport support;

    @Before
    public void before() {
        ExternalNfcServiceCallback callback = new ExternalNfcServiceCallback() {
            @Override
            public void onExternalNfcServiceStopped(Intent intent) {
                stopped.incrementAndGet();
            }

            @Override
            public void onExternalNfcServiceStarted(Intent intent) {
                started.incrementAndGet();
            }
        };

        context = new RecordingService(InstrumentationRegistry.getInstrumentation().getTargetContext(), null);
        support = new ExternalNfcServiceCallbackSupport(callback, context, Runnable::run, false);
    }

    @Test
    public void testCallbacksInvokedOnce() {
        support.setEnabled(true);

        context.deliver(new Intent(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));
        assertEquals(1, started.get());

        context.deliver(new Intent(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED));
        assertEquals(1, stopped.get());
    }

    @Test
    public void testResumeWhenDisabled() {
        support.onResume();

        assertFalse(context.isRegistered(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));
    }

    @Test
    public void testPauseAndResumeWhenEnabled() {
        support.setEnabled(true);
        assertTrue(context.isRegistered(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));

        support.onPause();
        assertFalse(context.isRegistered(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));

        support.onResume();
        assertTrue(context.isRegistered(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));

        support.setEnabled(false);
        support.onResume();
        assertFalse(context.isRegistered(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED));
    }
}
