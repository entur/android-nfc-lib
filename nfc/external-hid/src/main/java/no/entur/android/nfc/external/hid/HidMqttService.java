package no.entur.android.nfc.external.hid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hwb.utilities.mqtt3.client.MqttServiceClient;

public class HidMqttService extends Service implements MqttClientConnectedListener, MqttClientDisconnectedListener {

    public static final String MQTT_CLIENT_PORT = "PORT";
    public static final String MQTT_CLIENT_HOST = "HOST";
    public static final String MQTT_CLIENT_IDENTIFIER = "IDENTIFIER";

    public static final String MQTT_CLIENT_DEFAULT_WEBSOCKET_CONFIGURATION = "DEFAULT_WEBSOCKET_CONFIGURATION";

    public static final String MQTT_CLIENT_CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    public static final String MQTT_CLIENT_TRANSCEIVE_TIMEOUT = "TRANSCEIVE_TIMEOUT";

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    public static final String MQTT_CLIENT_RECONNECT_INITIAL_DELAY = "RECONNECT_INITIAL_DELAY";
    public static final String MQTT_CLIENT_RECONNECT_MAX_DELAY = "RECONNECT_MAX_DELAY";

    private static final Logger LOGGER = LoggerFactory.getLogger(HidMqttService.class);

    protected Atr210MqttHandler handler;

    public class LocalBinder extends Binder {
        public HidMqttService getService() {
            return HidMqttService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.info("Starting " + HidMqttService.class.getName() + " service");

        if(handler == null) {
            MqttServiceClient mqttServiceClient = createMqttServiceClient(intent);

            long transceiveTimeout = intent.getLongExtra(MQTT_CLIENT_TRANSCEIVE_TIMEOUT, 1000);

            handler = new Atr210MqttHandler(this, mqttServiceClient, transceiveTimeout);

            connect();
        }
        handler.broadcastStarted();

        return Service.START_STICKY;
    }

    public void connect() {
        if(handler != null) {
            try {
                if(handler.getClient().connect()) {
                    LOGGER.info("Connected to MQTT broker");
                } else {
                    LOGGER.warn("Not connected to MQTT broker");
                }
            } catch (Exception e) {
                LOGGER.warn("Problem connecting to MQTT broker", e);
            }
        }
    }

    public void disconnect() {
        if(handler != null) {
            try {
                handler.getClient().disconnect();
            } catch (Exception e) {
                LOGGER.warn("Problem disconnecting from MQTT broker", e);
            }
        }
    }

    @NonNull
    protected MqttServiceClient createMqttServiceClient(Intent intent) {
        // get MQTT client details

        int port = intent.getIntExtra(MQTT_CLIENT_PORT, 1183);

        String host = intent.getStringExtra(MQTT_CLIENT_HOST);
        if(host == null) {
            throw new IllegalStateException("Expect client host");
        }

        String identifier;
        if(intent.hasExtra(MQTT_CLIENT_IDENTIFIER)) {
            identifier = intent.getStringExtra(MQTT_CLIENT_IDENTIFIER);
        } else {
            identifier = UUID.randomUUID().toString();
        }

        boolean defaultConfiguration = intent.getBooleanExtra(MQTT_CLIENT_DEFAULT_WEBSOCKET_CONFIGURATION, false);

        Mqtt3ClientBuilder mqtt3ClientBuilder = Mqtt3Client.builder()
                .identifier(identifier)
                .serverPort(port)
                .serverHost(host);

        mqtt3ClientBuilder = configureAutomaticReconnect(intent, mqtt3ClientBuilder);

        if(defaultConfiguration) {
            mqtt3ClientBuilder = mqtt3ClientBuilder.webSocketWithDefaultConfig();
        }

        mqtt3ClientBuilder = mqtt3ClientBuilder.addConnectedListener(this);
        mqtt3ClientBuilder = mqtt3ClientBuilder.addDisconnectedListener(this);


        long timeout = intent.getLongExtra(MQTT_CLIENT_CONNECT_TIMEOUT, 5000);

        Mqtt3AsyncClient mqtt3AsyncClient = mqtt3ClientBuilder.buildAsync();

        Executor executor = Executors.newCachedThreadPool();

        MqttServiceClient mqttServiceClient = new MqttServiceClient(executor, mqtt3AsyncClient, timeout);
        return mqttServiceClient;
    }

    protected Mqtt3ClientBuilder configureAutomaticReconnect(Intent intent, Mqtt3ClientBuilder mqtt3ClientBuilder) {

        long initialDelay = intent.getLongExtra(MQTT_CLIENT_RECONNECT_INITIAL_DELAY, 500);
        long maxDelay = intent.getLongExtra(MQTT_CLIENT_RECONNECT_MAX_DELAY, 30_000);

        return mqtt3ClientBuilder.automaticReconnect()
                .initialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .maxDelay(maxDelay, TimeUnit.MILLISECONDS)
                .applyAutomaticReconnect();
    }

    public void onDestroy() {

        if(handler != null) {
            handler.broadcastStopped();
            handler.onDestroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOGGER.debug("Bind for intent " + intent.getAction());

        return new LocalBinder();
    }

    @Override
    public void onConnected(@NotNull MqttClientConnectedContext context) {
        handler.onConnected();
    }

    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        handler.onDisconnected();
    }

    public Atr210MqttHandler getHandler() {
        return handler;
    }

}
