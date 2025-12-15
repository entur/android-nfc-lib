package no.entur.android.nfc.external.atr210;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.atr210.intent.Atr210Service;
import no.entur.android.nfc.external.atr210.intent.bind.Atr210ServiceBinder;
import no.entur.android.nfc.external.atr210.intent.command.Atr210ServiceCommandsWrapper;
import no.entur.android.nfc.external.atr210.reader.Atr210ReaderContext;
import no.entur.android.nfc.external.atr210.reader.Atr210ReaderService;
import no.entur.android.nfc.external.atr210.schema.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class Atr210MqttService implements MqttClientDisconnectedListener {

    private static final String TOPIC_HEARTBEAT = "itxpt/inventory/providers/+/heartbeat/relative";

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MqttService.class);
    protected final Atr210Service atr210Service;

    protected Map<String, Atr210ReaderService> readers = new ConcurrentHashMap<>();

    protected final long transcieveTimeout;
    protected final Context context;
    protected final MqttServiceClient client;

    protected final TagProxyStore tagProxyStore = new DefaultTagProxyStore();

    public Atr210MqttService(Context context, MqttServiceClient client, long transcieveTimeout) {
        this.context = context;
        this.client = client;
        this.transcieveTimeout = transcieveTimeout;

        Atr210ServiceBinder binder = new Atr210ServiceBinder();
        binder.setServiceCommandsWrapper(new Atr210ServiceCommandsWrapper(this));
        this.atr210Service = new Atr210Service("ATR210", binder);
    }

    public void addReader(String deviceId) {
        spawnReader(deviceId);
    }

    public boolean connect() throws Exception {
        if(client.connect()) {
            onConnected();

            return true;
        }
        return false;
    }

    public void onConnected() {
        subscribe();

        broadcastStarted();

        // do not discover readers here, rather listen to heartbeats
    }

    public void disconnect() {
        try {
            unsubscribe();

            client.disconnect();
        } finally {
            onDisconnected();
        }
    }

    public void onDisconnected() {
        try {
            synchronized (readers) {
                for (Map.Entry<String, Atr210ReaderService> entry : readers.entrySet()) {
                    Atr210ReaderService value = entry.getValue();
                    value.close();
                }
            }
        } finally {
            broadcastStopped();
        }
    }

    public void broadcastStarted() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
        intent.putExtra(ExternalNfcServiceCallback.EXTRA_SERVICE_CONTROL, atr210Service);

        context.sendBroadcast(intent, Atr210MqttService.ANDROID_PERMISSION_NFC);
    }

    public void broadcastStopped() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

        context.sendBroadcast(intent, Atr210MqttService.ANDROID_PERMISSION_NFC);
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

        Atr210ReaderService atr210ReaderService = new Atr210ReaderService(context, client, readerContext, transcieveTimeout, tagProxyStore);
        readers.put(deviceId, atr210ReaderService);

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

                    scheduleHeartbeatCheck();
                }
            }
        }
        return atr210ReaderService;
    }

    private void scheduleHeartbeatCheck() {
        // TODO remove readers if no more heartbeat
    }

    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        // all readers disconnected
        onDisconnected();
    }

    public Set<String> getReaderIds() {
        return readers.keySet();
    }
}
