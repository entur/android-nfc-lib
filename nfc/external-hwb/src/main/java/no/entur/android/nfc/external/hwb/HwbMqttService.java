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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import hwb.utilities.device.diagnostics.request.RequestSchema;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hwb.card.HwbTagProxy;
import no.entur.android.nfc.external.hwb.intent.HwbService;
import no.entur.android.nfc.external.hwb.intent.bind.HwbServiceBinder;
import no.entur.android.nfc.external.hwb.intent.command.HwbServiceCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.HwbReaderContext;
import no.entur.android.nfc.external.hwb.reader.HwbReaderService;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class HwbMqttService implements MqttClientDisconnectedListener {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbMqttService.class);
    protected final HwbService hwbService;

    protected Map<String, HwbReaderService> readers = new ConcurrentHashMap<>();

    protected SynchronizedRequestResponseMessages<UUID> apduRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final long transcieveTimeout;
    protected final Context context;
    protected final HwbMqttClient client;

    protected final TagProxyStore tagProxyStore = new DefaultTagProxyStore();

    public HwbMqttService(Context context, HwbMqttClient client, long transcieveTimeout) {
        this.context = context;
        this.client = client;
        this.transcieveTimeout = transcieveTimeout;

        HwbServiceBinder binder = new HwbServiceBinder();
        binder.setServiceCommandsWrapper(new HwbServiceCommandsWrapper(this));
        this.hwbService = new HwbService("HWB", binder);
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
                for (Map.Entry<String, HwbReaderService> entry : readers.entrySet()) {
                    HwbReaderService value = entry.getValue();
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
        intent.putExtra(ExternalNfcServiceCallback.EXTRA_SERVICE_CONTROL, hwbService);

        context.sendBroadcast(intent, HwbMqttService.ANDROID_PERMISSION_NFC);
    }

    public void broadcastStopped() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

        context.sendBroadcast(intent, HwbMqttService.ANDROID_PERMISSION_NFC);
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

    public HwbReaderService addReader(String deviceId, DiagnosticsSchema receiveSchema) {
        HwbReaderContext readerContext = new HwbReaderContext();
        readerContext.setDeviceId(deviceId);
        readerContext.setDiagnosticsSchema(receiveSchema);

        HwbReaderService hwbReaderService = new HwbReaderService(context, client, apduRequestResponseMessages, readerContext, transcieveTimeout, tagProxyStore);
        readers.put(deviceId, hwbReaderService);

        return hwbReaderService;
    }

    public void onNewCard(NfcSchema schema) {
        HwbReaderService hwbReaderService = spawnReader(schema.getDeviceId());

        hwbReaderService.newTag(schema);
    }

    @NonNull
    private HwbReaderService spawnReader(String deviceId) {
        // do we have this reader? If not then create

        HwbReaderService hwbReaderService = readers.get(deviceId);
        if(hwbReaderService == null) {
            synchronized (readers) {
                hwbReaderService = readers.get(deviceId);
                if (hwbReaderService == null) {

                    // TODO send diagnostics message for this reader here and now?

                    // add as a generic reader, we do not know which type yet
                    hwbReaderService = addReader(deviceId, null);

                    hwbReaderService.open();
                }
            }
        }
        return hwbReaderService;
    }

    public void adpuResponse(ReceiveSchema receiveSchema) {
        try {
            HwbReaderService hwbReaderService = readers.get(receiveSchema.getDeviceId());

            if(hwbReaderService == null) {
                LOGGER.warn("Got ADPU response for unknown reader {}", receiveSchema.getDeviceId());
                return;
            }

            hwbReaderService.onAdpuResponse(receiveSchema);
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
}
