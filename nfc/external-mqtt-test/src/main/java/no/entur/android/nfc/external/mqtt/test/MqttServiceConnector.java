package no.entur.android.nfc.external.mqtt.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.core.util.Consumer;

import java.util.Properties;

public class MqttServiceConnector {

    protected final Properties configuration;
    protected Context context;

    /** Flag indicating whether we have called bind on the service. */
    protected boolean bound;

    protected MqttService.LocalBinder service;

    protected long timeout;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttServiceConnector.this.service = (MqttService.LocalBinder)service;
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
            bound = false;
        }
    };

    public MqttServiceConnector(Context context, long timeout) {
        this(context, null);
        this.timeout = timeout;
    }

    public MqttServiceConnector(Context context, Properties configuration) {
        this.context = context;
        this.configuration = configuration;
    }

    public void run(Consumer<MqttService> consumer) throws Exception {
        try {
            bind();

            if(waitForBind()) {
                consumer.accept(service.getService());
            } else {
                throw new IllegalStateException("Service not bound");
            }
        } finally {
            unbind();
        }
    }

    public void start() throws Exception {
        Intent intent = new Intent(context, MqttService.class);
        if (configuration != null && !configuration.isEmpty()) {
            intent.putExtra(MqttService.EXTRA_CONFIGURATION, configuration);
        }
        context.startService(intent);
    }

    public void stop() throws Exception {
        Intent intent = new Intent(context, MqttService.class);
        context.stopService(intent);
    }

    public void bind() {
        context.bindService(new Intent(context, MqttService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void unbind() {
        if(connection != null) {
            context.unbindService(connection);
        }
    }

    public boolean waitForBind() throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout;

        do {
            Thread.sleep(50);
            if(bound) {
                return true;
            }
        } while(System.currentTimeMillis() < deadline);

        return bound;
    }

}
