package no.entur.android.nfc.external.mqtt.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Properties;

public class MqttBrokerServiceConnector {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private long timeout = 1000L;
        private int port = -1;

        private Context context;

        public Builder withTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public MqttBrokerServiceConnector build() {
            Properties properties = new Properties();
            if(port != -1) {
                properties.put(MqttBrokerService.PORT, port);
            }

            return new MqttBrokerServiceConnector(context, properties, timeout);
        }

    }

    protected final Properties configuration;
    protected final Context context;

    /** Flag indicating whether we have called bind on the service. */
    protected boolean bound;

    protected MqttBrokerService.LocalBinder service;

    protected long timeout;
    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttBrokerServiceConnector.this.service = (MqttBrokerService.LocalBinder)service;
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
            bound = false;
        }
    };

    public MqttBrokerServiceConnector(Context context, long timeout) {
        this(context, null, timeout);
    }

    public MqttBrokerServiceConnector(Context context, Properties configuration, long timeout) {
        this.context = context;
        this.configuration = configuration;
        this.timeout = timeout;
    }

    public MqttBrokerServiceConnection connect() {
        return connect(false);
    }

    public MqttBrokerServiceConnection connect(boolean stopServiceOnClose) {
        if(!bound) {
            start();

            bind();

            if (!waitForBindToComplete()) {
                throw new IllegalStateException("Service not bound");
            }
        }
        return new DefaultMqttBrokerServiceConnection(this, service.getService(), stopServiceOnClose);
    }

    public void start() {
        Intent intent = new Intent(context, MqttBrokerService.class);
        if (configuration != null && !configuration.isEmpty()) {
            intent.putExtra(MqttBrokerService.EXTRA_CONFIGURATION, configuration);
        }
        context.startService(intent);
    }

    public void stop() {
        Intent intent = new Intent(context, MqttBrokerService.class);
        context.stopService(intent);
    }

    public void bind() {
        if(!bound) {
            context.bindService(new Intent(context, MqttBrokerService.class), connection, Context.BIND_AUTO_CREATE);
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
