package no.entur.android.nfc.external.hwb.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.core.util.Consumer;

import java.util.Properties;

public class MqttServiceConnector {

    private final Properties configuration;
    private Context context;

    /** Flag indicating whether we have called bind on the service. */
    private boolean bound;

    private MqttService.LocalBinder service;
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

    public MqttServiceConnector(Context context) {
        this(context, null);
    }

    public MqttServiceConnector(Context context, Properties configuration) {
        this.context = context;
        this.configuration = configuration;
    }

    public void run(Consumer<MqttService> consumer) throws Exception {
        Intent intent = new Intent(context, MqttService.class);
        if(configuration != null && !configuration.isEmpty()) {
            intent.putExtra(MqttService.EXTRA_CONFIGURATION, configuration);
        }
        context.startService(intent);

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

    public boolean waitForBind() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 1000;

        do {
            Thread.sleep(50);
            if(bound) {
                return true;
            }
        } while(System.currentTimeMillis() < deadline);

        return bound;
    }

    public void bind() {
        context.bindService(new Intent(context, MqttService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void unbind() {
        if(connection != null) {
            context.unbindService(connection);
        }
    }


}
