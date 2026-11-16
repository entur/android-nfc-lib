package no.entur.android.nfc.external.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Instrumentation;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.RecordingService;

@RunWith(AndroidJUnit4.class)
public class ExternalUsbNfcServiceSupportTest {

    private static final long TIMEOUT = 5000;

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    private final Map<String, UsbDevice> devices = new HashMap<>();
    private final Object reader = new Object();
    private final CountDownLatch openLatch = new CountDownLatch(1);

    private UsbDevice device;
    private ExternalUsbNfcServiceSupport.ReaderAdapter readerAdapter;
    private ExternalNfcReaderStatusListener listener;
    private RecordingService service;
    private ExternalUsbNfcServiceSupport support;

    @Before
    public void setUp() {
        device = mock(UsbDevice.class);
        when(device.getDeviceId()).thenReturn(1002);
        when(device.getDeviceName()).thenReturn("/dev/bus/usb/001/002");
        devices.put(device.getDeviceName(), device);

        UsbManager usbManager = mock(UsbManager.class);
        when(usbManager.getDeviceList()).thenAnswer(i -> new HashMap<>(devices));
        when(usbManager.hasPermission(any(UsbDevice.class))).thenReturn(true);

        readerAdapter = mock(ExternalUsbNfcServiceSupport.ReaderAdapter.class);
        when(readerAdapter.isSupportedDevice(any(), any())).thenReturn(true);
        when(readerAdapter.openReader(device)).thenReturn(reader);

        listener = mock(ExternalNfcReaderStatusListener.class);

        service = new RecordingService(instrumentation.getTargetContext(), usbManager);
        support = new ExternalUsbNfcServiceSupport(service, listener, readerAdapter, false);

        instrumentation.runOnMainSync(() -> support.onCreate());
    }

    @After
    public void tearDown() {
        openLatch.countDown();

        instrumentation.runOnMainSync(() -> support.onDestroy());
    }

    @Test
    public void testDetachClosesReader() {
        startScanning();

        verify(listener, timeout(TIMEOUT)).onReaderOpen(reader, ExternalNfcReaderCallback.READER_STATUS_OK);

        detach();

        verify(readerAdapter, timeout(TIMEOUT)).closeReader(device);
        verify(listener, timeout(TIMEOUT)).onReaderClosed(ExternalNfcReaderCallback.READER_STATUS_USB_DEVICE_DISCONNECTED, null);
    }

    @Test
    public void testPermissionBroadcastForOpenReaderDoesNotOpenAgain() {
        blockOpenReader();

        startScanning();

        // scanner found the device with permission already granted, and is opening it
        verify(readerAdapter, timeout(TIMEOUT)).openReader(device);

        Intent intent = new Intent(ExternalUsbNfcServiceSupport.ACTION_USB_PERMISSION);
        intent.putExtra(UsbManager.EXTRA_DEVICE, device);
        intent.putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true);
        deliver(intent);

        openLatch.countDown();

        verify(listener, timeout(TIMEOUT)).onReaderOpen(reader, ExternalNfcReaderCallback.READER_STATUS_OK);
        verify(readerAdapter, after(1000)).openReader(device);
    }

    @Test
    public void testDetachWhileOpeningClosesReader() {
        blockOpenReader();

        startScanning();

        verify(readerAdapter, timeout(TIMEOUT)).openReader(device);

        detach();

        openLatch.countDown();

        verify(readerAdapter, timeout(TIMEOUT)).closeReader(device);

        InOrder inOrder = inOrder(readerAdapter);
        inOrder.verify(readerAdapter).openReader(device);
        inOrder.verify(readerAdapter).closeReader(device);
    }

    @Test
    public void testDestroyWhileOpeningClosesReader() {
        blockOpenReader();

        startScanning();

        verify(readerAdapter, timeout(TIMEOUT)).openReader(device);

        instrumentation.runOnMainSync(() -> support.onDestroy());

        openLatch.countDown();

        verify(readerAdapter, timeout(TIMEOUT)).closeReader(device);
        verify(listener, after(1000).never()).onReaderOpen(any(), anyInt());

        assertEquals(0, service.getReceiverCount());
    }

    private void blockOpenReader() {
        when(readerAdapter.openReader(device)).thenAnswer(i -> {
            assertTrue(openLatch.await(TIMEOUT, TimeUnit.MILLISECONDS));
            return reader;
        });
    }

    private void startScanning() {
        instrumentation.runOnMainSync(() -> support.scanForDevices());
    }

    private void detach() {
        devices.remove(device.getDeviceName());

        Intent intent = new Intent(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intent.putExtra(UsbManager.EXTRA_DEVICE, device);

        assertTrue(service.isRegistered(UsbManager.ACTION_USB_DEVICE_DETACHED));
        deliver(intent);
    }

    private void deliver(Intent intent) {
        int[] count = new int[1];
        instrumentation.runOnMainSync(() -> count[0] = service.deliver(intent));
        assertFalse("No receiver for " + intent.getAction(), count[0] == 0);
    }
}
