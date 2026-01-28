package no.entur.android.nfc.external.hid.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import no.entur.android.nfc.external.hid.HidMqttService;
import no.entur.android.nfc.external.mqtt.test.MqttBrokerService;

public class HidServiceConnector {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        protected int port = -1;
        protected String host;
        protected String identifier;

        protected boolean defaultWebsocketConfiguration;

        protected long transceiveTimeout = -1L;
        protected long connectTimeout = -1L;

        protected long reconnectInitialDelay = -1L;
        protected long reconnectMaxDelay = -1L;

        protected long timeout = 1000L;

        protected Context context;

        protected boolean logApdus = false;

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withLogApdus(boolean logApdus) {
            this.logApdus = logApdus;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withDefaultWebsocketConfiguration() {
            this.defaultWebsocketConfiguration = true;
            return this;
        }

        public Builder withTransceiveTimeout(long transceiveTimeout) {
            this.transceiveTimeout = transceiveTimeout;
            return this;
        }

        public Builder withConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withReconnectInitialDelay(long reconnectInitialDelay) {
            this.reconnectInitialDelay = reconnectInitialDelay;
            return this;
        }

        public Builder withReconnectMaxDelay(long reconnectMaxDelay) {
            this.reconnectMaxDelay = reconnectMaxDelay;
            return this;
        }

        public Builder withTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public HidServiceConnector build() {
            if(host == null) {
                throw new IllegalStateException("Expected host");
            }
            return new HidServiceConnector(context, timeout, port, host, identifier, defaultWebsocketConfiguration, transceiveTimeout, connectTimeout, reconnectInitialDelay, reconnectMaxDelay, logApdus);
        }

    }

    protected final Context context;

    /** Flag indicating whether we have called bind on the service. */
    protected boolean bound;

    protected HidMqttService.LocalBinder service;

    protected final long timeout;

    protected final int port;
    protected final String host;
    protected final String identifier;

    protected final boolean defaultWebsocketConfiguration;

    protected final long transceiveTimeout;
    protected final long connectTimeout;

    protected final long reconnectInitialDelay;
    protected final long reconnectMaxDelay;

    protected final boolean logApdus;

    /**
     * Class for interacting with the main interface of the service.
     */
    protected final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            HidServiceConnector.this.service = (HidMqttService.LocalBinder)service;
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
            bound = false;
        }
    };

    public HidServiceConnector(Context context, long timeout, int port, String host, String identifier, boolean defaultWebsocketConfiguration, long transceiveTimeout, long connectTimeout, long reconnectInitialDelay, long reconnectMaxDelay, boolean logApdus) {
        this.context = context;
        this.timeout = timeout;
        this.port = port;
        this.host = host;
        this.identifier = identifier;
        this.defaultWebsocketConfiguration = defaultWebsocketConfiguration;
        this.transceiveTimeout = transceiveTimeout;
        this.connectTimeout = connectTimeout;
        this.reconnectInitialDelay = reconnectInitialDelay;
        this.reconnectMaxDelay = reconnectMaxDelay;
        this.logApdus = logApdus;
    }

    public HidServiceConnection connect() {
        return connect(false);
    }

    public HidServiceConnection connect(boolean stopServiceOnClose) {
        if(!bound) {
            start();

            bind();

            if (!waitForBindToComplete()) {
                throw new IllegalStateException("Service not bound after " + timeout);
            }
        }
        return new HidServiceConnection(this, service.getService(), stopServiceOnClose);
    }

    public void start() {
        if(host == null) {
            throw new IllegalStateException();
        }

        Intent intent = new Intent(context, HidMqttService.class);

        intent.putExtra(HidMqttService.MQTT_CLIENT_HOST, host);

        if(port != -1) {
            intent.putExtra(HidMqttService.MQTT_CLIENT_PORT, port);
        }

        if(transceiveTimeout != -1L) {
            intent.putExtra(HidMqttService.MQTT_CLIENT_TRANSCEIVE_TIMEOUT, transceiveTimeout);
        }

        if(connectTimeout != -1L) {
            intent.putExtra(HidMqttService.MQTT_CLIENT_CONNECT_TIMEOUT, connectTimeout);
        }

        if(reconnectInitialDelay != -1L) {
            intent.putExtra(HidMqttService.MQTT_CLIENT_RECONNECT_INITIAL_DELAY, reconnectInitialDelay);
        }

        if(reconnectMaxDelay != -1L) {
            intent.putExtra(HidMqttService.MQTT_CLIENT_RECONNECT_MAX_DELAY, reconnectMaxDelay);
        }

        if(logApdus) {
            intent.putExtra(HidMqttService.LOG_APDUS, true);
        }

        intent.putExtra(HidMqttService.MQTT_CLIENT_DEFAULT_WEBSOCKET_CONFIGURATION, defaultWebsocketConfiguration);

        context.startService(intent);
    }

    public void stop() {
        Intent intent = new Intent(context, HidMqttService.class);
        context.stopService(intent);
    }

    public void bind() {
        if(!bound) {
            context.bindService(new Intent(context, HidMqttService.class), connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbind() {
        if(connection != null) {
            context.unbindService(connection);
        }
    }

    public boolean waitForBindToComplete() {
        long deadline = System.currentTimeMillis() + timeout;

        do {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return bound;
            }
            if(bound) {
                return true;
            }
        } while(System.currentTimeMillis() < deadline);

        return bound;
    }

}
