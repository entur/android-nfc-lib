package no.entur.android.nfc.external.hwb;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hwb.intent.Atr210Service;
import no.entur.android.nfc.external.hwb.intent.bind.Atr210ServiceBinder;
import no.entur.android.nfc.external.hwb.intent.command.Atr210ServiceCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.Atr210ReaderContext;
import no.entur.android.nfc.external.hwb.reader.Atr210ReaderService;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class Atr210MqttService implements MqttClientDisconnectedListener {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MqttService.class);
    protected final Atr210Service atr210Service;

    protected Map<String, Atr210ReaderService> readers = new ConcurrentHashMap<>();

    protected SynchronizedRequestResponseMessages<UUID> apduRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

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
        this.atr210Service = new Atr210Service("HWB", binder);
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

        discoverReaders();
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
        // /device/diagnostics <- new readers
        // /validators/nfc <- new tags
        // /validators/nfc/apdu/receive <- response to ADPU messages

        client.subscribe("/validators/nfc", NfcSchema.class, this::onNewCard);
        client.subscribe("/validators/diagnostics", DiagnosticsSchema.class, this::onDiagnostics);
        client.subscribe("/validators/nfc/apdu/receive", ReceiveSchema.class, this::adpuResponse);
    }

    public void unsubscribe() {
        // subscribes to topics
        // /device/diagnostics <- new readers
        // /validators/nfc <- new tags

        client.unsubscribe("/validators/nfc");
        client.unsubscribe("/device/diagnostics");
        client.unsubscribe("/validators/nfc/apdu/receive");
    }

    public void onDiagnostics(DiagnosticsSchema receiveSchema) {
        List<Object> functionality = receiveSchema.getFunctionality();
        if (functionality != null && functionality.contains("nfc")) {

            synchronized (readers) {
                if (!readers.containsKey(receiveSchema.getDeviceId())) {
                    addReader(receiveSchema.getDeviceId(), receiveSchema);
                } else {
                    LOGGER.info("Already have reader " + receiveSchema.getDeviceId());
                }
            }
        } else {
            LOGGER.info("Not adding non-NFC reader " + receiveSchema.getDeviceId());
        }
    }

    public Atr210ReaderService addReader(String deviceId, DiagnosticsSchema receiveSchema) {
        Atr210ReaderContext readerContext = new Atr210ReaderContext();
        readerContext.setDeviceId(deviceId);
        readerContext.setDiagnosticsSchema(receiveSchema);

        Atr210ReaderService atr210ReaderService = new Atr210ReaderService(context, client, apduRequestResponseMessages, readerContext, transcieveTimeout, tagProxyStore);
        readers.put(deviceId, atr210ReaderService);

        return atr210ReaderService;
    }

    public void onNewCard(NfcSchema schema) {
        Atr210ReaderService atr210ReaderService = spawnReader(schema.getDeviceId());

        atr210ReaderService.newTag(schema);
    }

    @NonNull
    private Atr210ReaderService spawnReader(String deviceId) {
        // do we have this reader? If not then create

        Atr210ReaderService atr210ReaderService = readers.get(deviceId);
        if(atr210ReaderService == null) {
            synchronized (readers) {
                atr210ReaderService = readers.get(deviceId);
                if (atr210ReaderService == null) {

                    // TODO send diagnostics message for this reader here and now?

                    // add as a generic reader, we do not know which type yet
                    atr210ReaderService = addReader(deviceId, null);

                    atr210ReaderService.open();
                }
            }
        }
        return atr210ReaderService;
    }

    public void adpuResponse(ReceiveSchema receiveSchema) {
        try {
            Atr210ReaderService atr210ReaderService = readers.get(receiveSchema.getDeviceId());

            if(atr210ReaderService == null) {
                LOGGER.warn("Got ADPU response for unknown reader {}", receiveSchema.getDeviceId());
                return;
            }

            atr210ReaderService.onAdpuResponse(receiveSchema);
        } catch (Exception e) {
            LOGGER.error("Problem handling ADPU MQTT response message", e);
        }
    }

    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        // all readers disconnected
        onDisconnected();
    }

    public void discoverReaders() {
        try {
            // ping readers
            // https://github.com/entur/hwb/blob/main/specifications/device/diagnostics/request/request.md

            RequestSchema requestSchema = new RequestSchema();
            requestSchema.setTraceId(UUID.randomUUID());
            requestSchema.setEventTimestamp(new Date());

            client.publish("/device/diagnostics/request", requestSchema);
        } catch (Exception e) {
            LOGGER.error("Problem discovering MQTT NFC readers", e);
        }
    }

    public Set<String> getReaderIds() {
        return readers.keySet();
    }
}
