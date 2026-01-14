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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import hwb.utilities.device.diagnostics.request.RequestSchema;
import hwb.utilities.validators.barcode.BarcodeSchema;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hwb.intent.HwbService;
import no.entur.android.nfc.external.hwb.intent.bind.HwbServiceBinder;
import no.entur.android.nfc.external.hwb.intent.command.HwbServiceCommandsWrapper;
import no.entur.android.nfc.external.hwb.reader.HwbReaderContext;
import no.entur.android.nfc.external.hwb.reader.HwbReaderService;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

public class HwbMqttHandler implements MqttClientDisconnectedListener {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbMqttHandler.class);
    protected final HwbService hwbService;
    protected final HwbVerifyHeartbeatTimer verifyHeartbeatTimer;
    protected final HwbRequestHeartbeatTimer requestHeartbeatTimer;
    protected final Map<String, HwbReaderService> readers = new ConcurrentHashMap<>();

    protected SynchronizedRequestResponseMessages<UUID> apduRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final long transcieveTimeout;
    protected final Context context;
    protected final MqttServiceClient client;

    protected final TagProxyStore tagProxyStore = new DefaultTagProxyStore();

    protected boolean connected = false;

    public HwbMqttHandler(Context context, MqttServiceClient client, long transcieveTimeout) {
        this.context = context;
        this.client = client;
        this.transcieveTimeout = transcieveTimeout;

        HwbServiceBinder binder = new HwbServiceBinder();
        binder.setServiceCommandsWrapper(new HwbServiceCommandsWrapper(this));
        this.hwbService = new HwbService("HWB", binder);

        this.verifyHeartbeatTimer = new HwbVerifyHeartbeatTimer(5000, this);
        this.requestHeartbeatTimer = new HwbRequestHeartbeatTimer(verifyHeartbeatTimer.getIntervalInMillis(), this);
    }

    public void onConnected() {
        connected = true;

        subscribe();
        discoverReaders();
    }

    public void onDisconnected() {
        connected = false;

        unsubscribe(); // TODO is this necessary?

        synchronized (readers) {
            for (Map.Entry<String, HwbReaderService> entry : readers.entrySet()) {
                HwbReaderService value = entry.getValue();
                value.close();
            }
            readers.clear();

            verifyHeartbeatTimer.cancel();
            requestHeartbeatTimer.cancel();
        }
    }

    public void subscribe() {
        // subscribes to topics
        // /device/diagnostics <- new readers
        // /validators/nfc <- new tags
        // /validators/nfc/apdu/receive <- response to ADPU messages
        // /validators/barcode <- new barcodes

        client.subscribeToJson("/validators/barcode", this::onBarcode, BarcodeSchema.class);

        client.subscribeToJson("/validators/nfc", this::onNewCard, NfcSchema.class);
        client.subscribeToJson("/validators/diagnostics", this::onHeartbeat, DiagnosticsSchema.class);
        client.subscribeToJson("/validators/nfc/apdu/receive", this::adpuResponse, ReceiveSchema.class);
    }

    public void unsubscribe() {
        // subscribes to topics
        // /device/diagnostics <- new readers
        // /validators/nfc <- new tags

        client.unsubscribe("/validators/barcode");

        client.unsubscribe("/validators/nfc");
        client.unsubscribe("/device/diagnostics");
        client.unsubscribe("/validators/nfc/apdu/receive");
    }

    public HwbReaderService addReader(String id) {
        HwbReaderContext readerContext = new HwbReaderContext();
        readerContext.setDeviceId(id);

        HwbReaderService hwbReaderService = new HwbReaderService(context, client, apduRequestResponseMessages, readerContext, transcieveTimeout, tagProxyStore);
        readers.put(id, hwbReaderService);

        verifyHeartbeatTimer.schedule();
        requestHeartbeatTimer.schedule();

        return hwbReaderService;
    }

    public void onHeartbeat(DiagnosticsSchema heartbeat) {
        LOGGER.info("Got heartbeat for " + heartbeat.getDeviceId());

        List<Object> functionality = heartbeat.getFunctionality();
        if (functionality != null && functionality.contains("nfc")) {
            HwbReaderService hwbReaderService = spawnReader(heartbeat);

            hwbReaderService.setNextHeartbeatDeadline(System.currentTimeMillis() + verifyHeartbeatTimer.getIntervalInMillis());
        } else {
            LOGGER.info("Not adding non-NFC reader " + heartbeat.getDeviceId());
        }

    }

    @NonNull
    private HwbReaderService spawnReader(DiagnosticsSchema heartbeat) {
        // do we have this reader? If not then create

        HwbReaderService hwbReaderService = spawnReader(heartbeat.getDeviceId());
        hwbReaderService.setDiagnosticsSchema(heartbeat);

        return hwbReaderService;
    }
    @NonNull
    private HwbReaderService spawnReader(String id) {
        // do we have this reader? If not then create

        HwbReaderService atr210ReaderService = readers.get(id);
        if(atr210ReaderService == null) {
            synchronized (readers) {
                atr210ReaderService = readers.get(id);
                if (atr210ReaderService == null) {
                    atr210ReaderService = addReader(id);
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

    public void onNewCard(NfcSchema schema) {
        HwbReaderService hwbReaderService = spawnReader(schema.getDeviceId());
        hwbReaderService.newTag(schema);
    }

    public void onBarcode(BarcodeSchema schema) {
        HwbReaderService hwbReaderService = spawnReader(schema.getDeviceId());
        hwbReaderService.barcode(schema);
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

    public void discoverReaders() {
        try {
            // ping readers
            // https://github.com/entur/hwb/blob/main/specifications/device/diagnostics/request/request.md

            RequestSchema requestSchema = new RequestSchema();
            requestSchema.setTraceId(UUID.randomUUID());
            requestSchema.setEventTimestamp(new Date());

            client.publishAsJson("/device/diagnostics/request", requestSchema);
        } catch (Exception e) {
            LOGGER.error("Problem discovering MQTT NFC readers", e);
        }
    }

    public Set<String> getReaderIds() {
        return readers.keySet();
    }

    public boolean verifyHeartbeats() {
        synchronized (readers) {

            Set<String> removed = new HashSet<>();
            for (Map.Entry<String, HwbReaderService> entry : readers.entrySet()) {

                HwbReaderService value = entry.getValue();
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
        verifyHeartbeatTimer.close();
        requestHeartbeatTimer.cancel();
    }

    public void broadcastStarted() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
        intent.putExtra(ExternalNfcServiceCallback.EXTRA_SERVICE_CONTROL, hwbService);

        context.sendBroadcast(intent, HwbMqttHandler.ANDROID_PERMISSION_NFC);
    }

    public void broadcastStopped() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

        context.sendBroadcast(intent, HwbMqttHandler.ANDROID_PERMISSION_NFC);
    }

    public MqttServiceClient getClient() {
        return client;
    }
    public HwbReaderService getReaderService(String id) {
        return readers.get(id);
    }

    public boolean isConnected() {
        return connected;
    }
}
