package no.entur.android.nfc.external.hwb.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import no.entur.android.nfc.external.hwb.HwbMqttService;

public class HwbServiceConnector {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        public static final String MQTT_CLIENT_PORT = "PORT";
        public static final String MQTT_CLIENT_HOST = "HOST";
        public static final String MQTT_CLIENT_IDENTIFIER = "IDENTIFIER";

        public static final String MQTT_CLIENT_DEFAULT_WEBSOCKET_CONFIGURATION = "DEFAULT_WEBSOCKET_CONFIGURATION";

        public static final String MQTT_CLIENT_CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
        public static final String MQTT_CLIENT_TRANSCEIVE_TIMEOUT = "TRANSCEIVE_TIMEOUT";

        public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

        protected static final String MQTT_CLIENT_RECONNECT_INITIAL_DELAY = "RECONNECT_INITIAL_DELAY";
        protected static final String MQTT_CLIENT_RECONNECT_MAX_DELAY = "RECONNECT_MAX_DELAY";

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

        public Builder withPort(int port) {
            this.port = port;
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

        public HwbServiceConnector build() {
            if(host == null) {
                throw new IllegalStateException("Expected host");
            }
            return new HwbServiceConnector(context, timeout, port, host, identifier, defaultWebsocketConfiguration, transceiveTimeout, connectTimeout, reconnectInitialDelay, reconnectMaxDelay);
        }

    }

    protected final Context context;

    /** Flag indicating whether we have called bind on the service. */
    protected boolean bound;

    protected HwbMqttService.LocalBinder service;

    protected final long timeout;

    protected final int port;
    protected final String host;
    protected final String identifier;

    protected final boolean defaultWebsocketConfiguration;

    protected final long transceiveTimeout;
    protected final long connectTimeout;

    protected final long reconnectInitialDelay;
    protected final long reconnectMaxDelay;


    /**
     * Class for interacting with the main interface of the service.
     */
    protected final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            HwbServiceConnector.this.service = (HwbMqttService.LocalBinder)service;
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
            bound = false;
        }
    };

    public HwbServiceConnector(Context context, long timeout, int port, String host, String identifier, boolean defaultWebsocketConfiguration, long transceiveTimeout, long connectTimeout, long reconnectInitialDelay, long reconnectMaxDelay) {
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
    }

    public HwbServiceConnection connect() {
        return connect(false);
    }

    public HwbServiceConnection connect(boolean stopServiceOnClose) {
        if(!bound) {
            start();

            bind();

            if (!waitForBindToComplete()) {
                throw new IllegalStateException("Service not bound after " + timeout);
            }
        }
        return new HwbServiceConnection(this, service.getService(), stopServiceOnClose);
    }

    public void start() {
        if(host == null) {
            throw new IllegalStateException();
        }

        Intent intent = new Intent(context, HwbMqttService.class);

        intent.putExtra(HwbMqttService.MQTT_CLIENT_HOST, host);

        if(port != -1) {
            intent.putExtra(HwbMqttService.MQTT_CLIENT_PORT, port);
        }

        if(transceiveTimeout != -1L) {
            intent.putExtra(HwbMqttService.MQTT_CLIENT_TRANSCEIVE_TIMEOUT, transceiveTimeout);
        }

        if(connectTimeout != -1L) {
            intent.putExtra(HwbMqttService.MQTT_CLIENT_CONNECT_TIMEOUT, connectTimeout);
        }

        if(reconnectInitialDelay != -1L) {
            intent.putExtra(HwbMqttService.MQTT_CLIENT_RECONNECT_INITIAL_DELAY, reconnectInitialDelay);
        }

        if(reconnectMaxDelay != -1L) {
            intent.putExtra(HwbMqttService.MQTT_CLIENT_RECONNECT_MAX_DELAY, reconnectMaxDelay);
        }

        intent.putExtra(HwbMqttService.MQTT_CLIENT_DEFAULT_WEBSOCKET_CONFIGURATION, defaultWebsocketConfiguration);

        context.startService(intent);
    }

    public void stop() {
        Intent intent = new Intent(context, HwbMqttService.class);
        context.stopService(intent);
    }

    public void bind() {
        if(!bound) {
            context.bindService(new Intent(context, HwbMqttService.class), connection, Context.BIND_AUTO_CREATE);
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
