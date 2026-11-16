package no.entur.android.nfc.external.mqtt.test;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import java.util.Properties;

import no.entur.android.nfc.external.service.AbstractForegroundService;

public class MqttBrokerServiceConnector {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private long timeout = 1000L;
        private int port = -1;

        private Context context;

        protected int notificationId;
        protected Notification notification;
        protected int serviceType;

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

        public Builder withForegroundService(int id, Notification notification, int serviceType) {
            this.notificationId = id;
            this.notification = notification;
            this.serviceType = serviceType;
            return this;
        }


        public MqttBrokerServiceConnector build() {
            Properties properties = new Properties();
            if(port != -1) {
                // must be a String, Properties.getProperty(..) ignores other value types
                properties.setProperty(MqttBrokerService.PORT, Integer.toString(port));
            }

            return new MqttBrokerServiceConnector(context, properties, timeout, notificationId, notification, serviceType);
        }

    }

    protected final Properties configuration;
    protected final Context context;

    /** Flag indicating whether the service is bound. */
    protected boolean bound;

    /** Flag indicating whether we have called bind on the service. */
    protected boolean bindRequested;

    protected MqttBrokerService.LocalBinder service;

    protected long timeout;

    protected final int notificationId;
    protected final Notification notification;
    protected final int serviceType;

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

    public MqttBrokerServiceConnector(Context context, long timeout, int notificationId, Notification notification, int serviceType) {
        this(context, null, timeout, notificationId, notification, serviceType);
    }

    public MqttBrokerServiceConnector(Context context, Properties configuration, long timeout, int notificationId, Notification notification, int serviceType) {
        this.context = context;
        this.configuration = configuration;
        this.timeout = timeout;
        this.notificationId = notificationId;
        this.notification = notification;
        this.serviceType = serviceType;
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

        if(notification != null) {
            intent.putExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION_ID, notificationId);
            intent.putExtra(AbstractForegroundService.FOREGROUND_NOTIFICATION, notification);
            intent.putExtra(AbstractForegroundService.FOREGROUND_SERVICE_TYPE, serviceType);
        }

        // only promise a foreground service if there is a notification to call startForeground(..) with
        if (notification != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void stop() {
        Intent intent = new Intent(context, MqttBrokerService.class);
        context.stopService(intent);
    }

    public void bind() {
        if(!bound) {
            bindRequested = true;
            context.bindService(new Intent(context, MqttBrokerService.class), connection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbind() {
        if(bindRequested) {
            bindRequested = false;
            context.unbindService(connection);
        }
        // onServiceDisconnected(..) is not called on unbind
        bound = false;
        service = null;
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
