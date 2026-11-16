package no.entur.android.nfc.external;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service backed by a real context, which records broadcast receivers instead of registering them,
 * so that tests can deliver (also protected) broadcasts directly. Optionally serves a custom {@link UsbManager}.
 */

public class RecordingService extends Service {

    private final Map<BroadcastReceiver, IntentFilter> receivers = new ConcurrentHashMap<>();

    private final UsbManager usbManager;

    public RecordingService(Context base, UsbManager usbManager) {
        attachBaseContext(base);
        this.usbManager = usbManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public Object getSystemService(String name) {
        if (usbManager != null && Context.USB_SERVICE.equals(name)) {
            return usbManager;
        }
        return super.getSystemService(name);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        receivers.put(receiver, filter);
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        receivers.put(receiver, filter);
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        receivers.put(receiver, filter);
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        receivers.put(receiver, filter);
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (receivers.remove(receiver) == null) {
            throw new IllegalArgumentException("Receiver not registered: " + receiver);
        }
    }

    public boolean isRegistered(String action) {
        for (IntentFilter filter : receivers.values()) {
            if (filter.hasAction(action)) {
                return true;
            }
        }
        return false;
    }

    public int getReceiverCount() {
        return receivers.size();
    }

    /**
     * Deliver the intent to the registered receivers matching its action, on the calling thread.
     *
     * @return number of receivers the intent was delivered to
     */

    public int deliver(Intent intent) {
        List<BroadcastReceiver> matching = new ArrayList<>();
        for (Map.Entry<BroadcastReceiver, IntentFilter> entry : receivers.entrySet()) {
            if (entry.getValue().hasAction(intent.getAction())) {
                matching.add(entry.getKey());
            }
        }
        for (BroadcastReceiver receiver : matching) {
            receiver.onReceive(this, intent);
        }
        return matching.size();
    }
}
