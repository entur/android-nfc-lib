package no.entur.android.nfc.external.hid;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hid.intent.HidService;
import no.entur.android.nfc.external.hid.intent.bind.Atr210ServiceBinder;
import no.entur.android.nfc.external.hid.intent.command.HidServiceCommandsWrapper;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderConfiguration;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderContext;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderService;
import no.entur.android.nfc.external.hid.dto.atr210.heartbeat.HeartbeatResponse;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxyStore;

/**
 *
 * Handler for ATR-210 readers over MQTT.
 *
 */


public class Atr210MqttHandler implements MqttClientDisconnectedListener {

    protected static final String TOPIC_HEARTBEAT = "itxpt/inventory/providers/+/heartbeat/relative";

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210MqttHandler.class);
    protected final HidService hidService;
    protected final Atr210HeartbeatTimer atr210HeartbeatTimer;

    protected Map<String, Atr210ReaderService> readers = new ConcurrentHashMap<>();

    protected final long transceiveTimeout;
    protected final Context context;
    protected final MqttServiceClient client;

    protected final TagProxyStore tagProxyStore = new DefaultTagProxyStore();

    protected Atr210ReaderConfiguration configuration;
    protected boolean connected = false;
    protected boolean logApdus;

    public Atr210MqttHandler(Context context, MqttServiceClient client, long transceiveTimeout, boolean logApdus) {
        this.context = context;
        this.client = client;
        this.transceiveTimeout = transceiveTimeout;
        this.logApdus = logApdus;

        this.configuration = new Atr210ReaderConfiguration(transceiveTimeout);

        Atr210ServiceBinder binder = new Atr210ServiceBinder();
        binder.setServiceCommandsWrapper(new HidServiceCommandsWrapper(this));
        this.hidService = new HidService("ATR210", binder);

        this.atr210HeartbeatTimer = new Atr210HeartbeatTimer(5000, this);
    }

    public void onConnected() {
        connected = true;

        subscribe();
        // do not discover readers here, rather listen to heartbeats

    }

    public void onDisconnected() {
        connected = false;

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

    public Atr210ReaderService addReader(HeartbeatResponse heartbeat) {
        Atr210ReaderContext readerContext = new Atr210ReaderContext();

        // FRATR210EH1234567890
        readerContext.setClientId("FR" + heartbeat.getDeviceType() + heartbeat.getDeviceId());

        // itxpt.ticketreader.ATR210EH.1234567890
        readerContext.setProviderId("itxpt.ticketreader." + heartbeat.getDeviceType() + "." + heartbeat.getDeviceId());
        readerContext.setHeartbeat(heartbeat);

        Atr210ReaderService atr210ReaderService = new Atr210ReaderService(context, client, readerContext, transceiveTimeout, tagProxyStore, logApdus);
        readers.put(heartbeat.getDeviceId(), atr210ReaderService);

        atr210HeartbeatTimer.schedule();

        return atr210ReaderService;
    }

    public void onHeartbeat(HeartbeatResponse heartbeat) {
        Atr210ReaderService atr210ReaderService = spawnReader(heartbeat);

        atr210ReaderService.setNextHeartbeatDeadline(System.currentTimeMillis() + heartbeat.getNextHeartbeatWithinSeconds().intValue() * 1000 + 1000);
    }

    @NonNull
    private Atr210ReaderService spawnReader(HeartbeatResponse heartbeat) {
        // do we have this reader? If not then create

        Atr210ReaderService atr210ReaderService = readers.get(heartbeat.getDeviceId());
        if(atr210ReaderService == null) {
            synchronized (readers) {
                atr210ReaderService = readers.get(heartbeat.getDeviceId());
                if (atr210ReaderService == null) {
                    atr210ReaderService = addReader(heartbeat);

                    configuration.open(atr210ReaderService);
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

    /**
     * @return reader ids, as in the reader opened / closed broadcasts and {@link no.entur.android.nfc.external.ExternalNfcReaderCallback#EXTRAS_READER_ID}
     */

    public Set<String> getReaderIds() {
        Set<String> ids = new HashSet<>();
        for (Atr210ReaderService atr210ReaderService : readers.values()) {
            ids.add(atr210ReaderService.getReaderId());
        }
        return ids;
    }

    public boolean verifyHeartbeats() {
        List<Atr210ReaderService> removed = new ArrayList<>();
        synchronized (readers) {
            Iterator<Map.Entry<String, Atr210ReaderService>> iterator = readers.entrySet().iterator();
            while (iterator.hasNext()) {
                Atr210ReaderService value = iterator.next().getValue();
                if(!value.hasHeartbeat()) {
                    iterator.remove();

                    removed.add(value);
                }
            }
        }

        // unsubscribe, so that a new reader service for the same reader (when it is back) does not get duplicate messages
        for (Atr210ReaderService atr210ReaderService : removed) {
            LOGGER.info("No heartbeat from reader " + atr210ReaderService.getReaderId() + ", closing");

            atr210ReaderService.close();
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

    public MqttServiceClient getClient() {
        return client;
    }

    public void enableNfcAutoNfcConfiguration(boolean hf, boolean sam) {
        configuration.setAutoConfiguration(true);
        configuration.setNfcReaders(hf, sam);
    }

    public void disableNfcAutoNfcConfiguration() {
        configuration.setAutoConfiguration(false);
    }

    /**
     * @param id reader id (see {@link #getReaderIds()}) or device id
     * @return the reader, or null
     */

    public Atr210ReaderService getReaderService(String id) {
        Atr210ReaderService atr210ReaderService = readers.get(id);
        if(atr210ReaderService != null) {
            return atr210ReaderService;
        }
        for (Atr210ReaderService readerService : readers.values()) {
            if(readerService.getReaderId().equals(id)) {
                return readerService;
            }
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setLogApdus(boolean logApdus) {
        if(logApdus != this.logApdus) {
            if (logApdus) {
                LOGGER.info("Enable APDU logging");
            } else {
                LOGGER.info("Disable APDU logging");
            }

            this.logApdus = logApdus;

            synchronized (readers) {
                for (Map.Entry<String, Atr210ReaderService> entry : readers.entrySet()) {
                    Atr210ReaderService value = entry.getValue();
                    value.setLogApdus(logApdus);
                }
            }
        }
    }
}
