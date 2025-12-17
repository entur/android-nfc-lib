package no.entur.android.nfc.external.hid;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hid.intent.HidService;
import no.entur.android.nfc.external.hid.intent.bind.Atr210ServiceBinder;
import no.entur.android.nfc.external.hid.intent.command.Atr210ServiceCommandsWrapper;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderContext;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderService;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;

public class Atr210MqttHandler implements MqttClientDisconnectedListener {

    private static final String TOPIC_HEARTBEAT = "itxpt/inventory/providers/+/heartbeat/relative";

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MqttHandler.class);
    protected final HidService hidService;
    protected final Atr210HeartbeatTimer atr210HeartbeatTimer;

    protected Map<String, Atr210ReaderService> readers = new ConcurrentHashMap<>();

    protected final long transceiveTimeout;
    protected final Context context;
    protected final MqttServiceClient client;

    protected final TagProxyStore tagProxyStore = new DefaultTagProxyStore();

    public Atr210MqttHandler(Context context, MqttServiceClient client, long transceiveTimeout) {
        this.context = context;
        this.client = client;
        this.transceiveTimeout = transceiveTimeout;

        Atr210ServiceBinder binder = new Atr210ServiceBinder();
        binder.setServiceCommandsWrapper(new Atr210ServiceCommandsWrapper(this));
        this.hidService = new HidService("ATR210", binder);

        this.atr210HeartbeatTimer = new Atr210HeartbeatTimer(5000, this);
    }

    public void addReader(String deviceId) {
        spawnReader(deviceId);
    }

    public void onConnected() {
        subscribe();
        // do not discover readers here, rather listen to heartbeats
    }

    public void onDisconnected() {
        unsubscribe(); // TODO is this necessary?

        synchronized (readers) {
            for (Map.Entry<String, Atr210ReaderService> entry : readers.entrySet()) {
                Atr210ReaderService value = entry.getValue();
                value.close();
            }
            readers.clear();

            atr210HeartbeatTimer.cancel();
        }
    }

    public void subscribe() {
        // subscribes to topics
        client.subscribeToJson(TOPIC_HEARTBEAT, this::onHeartbeat, HeartbeatResponse.class);
    }

    public void unsubscribe() {
        // subscribes to topics
        client.unsubscribe(TOPIC_HEARTBEAT);
    }

    public Atr210ReaderService addReader(String deviceId, HeartbeatResponse heartbeat) {
        Atr210ReaderContext readerContext = new Atr210ReaderContext();
        readerContext.setClientId(deviceId);
        readerContext.setHeartbeat(heartbeat);

        Atr210ReaderService atr210ReaderService = new Atr210ReaderService(context, client, readerContext, transceiveTimeout, tagProxyStore);
        readers.put(deviceId, atr210ReaderService);

        atr210HeartbeatTimer.schedule();

        return atr210ReaderService;
    }

    public void onHeartbeat(HeartbeatResponse heartbeat) {
        Atr210ReaderService atr210ReaderService = spawnReader(heartbeat.getDeviceId());

        atr210ReaderService.setNextHeartbeatDeadline(System.currentTimeMillis() + heartbeat.getNextHeartbeatWithinSeconds().intValue() * 1000 + 1000);
    }

    @NonNull
    private Atr210ReaderService spawnReader(String deviceId) {
        // do we have this reader? If not then create

        Atr210ReaderService atr210ReaderService = readers.get(deviceId);
        if(atr210ReaderService == null) {
            synchronized (readers) {
                atr210ReaderService = readers.get(deviceId);
                if (atr210ReaderService == null) {
                    atr210ReaderService = addReader(deviceId, null);

                    atr210ReaderService.open();
                }
            }
        }
        return atr210ReaderService;
    }

    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        // all readers disconnected
        onDisconnected();
    }

    public Set<String> getReaderIds() {
        return readers.keySet();
    }

    public boolean verifyHeartbeats() {
        synchronized (readers) {

            Set<String> removed = new HashSet<>();
            for (Map.Entry<String, Atr210ReaderService> entry : readers.entrySet()) {

                Atr210ReaderService value = entry.getValue();
                if(!value.hasHeartbeat()) {
                    removed.add(entry.getKey());

                    value.broadcastClosed();
                }
            }
            for (String s : removed) {
                readers.remove(s);
            }
        }

        return !readers.isEmpty();
    }

    public void onDestroy() {
        atr210HeartbeatTimer.close();
    }

    public void broadcastStarted() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
        intent.putExtra(ExternalNfcServiceCallback.EXTRA_SERVICE_CONTROL, hidService);

        context.sendBroadcast(intent, HidMqttService.ANDROID_PERMISSION_NFC);
    }

    public void broadcastStopped() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

        context.sendBroadcast(intent, HidMqttService.ANDROID_PERMISSION_NFC);
    }

}
