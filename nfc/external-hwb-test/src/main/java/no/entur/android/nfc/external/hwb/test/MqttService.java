package no.entur.android.nfc.external.hwb.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;

public class MqttService extends Service {

    public static final String EXTRA_CONFIGURATION = MqttService.class.getName() + ".EXTRA_CONFIGURATION";

    private static final String TAG = "MQTTService";

    private Server server = new Server();
    private boolean started = false;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }

        public Server getServer() {
            return server;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting Notification Service");

        startServer(intent);
        return START_STICKY;
    }

    private void startServer(Intent intent) {
        if(!started) {
            started = true;

            Properties config = new Properties();
            if(intent.hasExtra(EXTRA_CONFIGURATION)) {
                Map<String, String> map = (HashMap) intent.getSerializableExtra(EXTRA_CONFIGURATION);
                config.putAll(map);
            } else {
                config.put(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, 8080);
                //config.put(IConfig.HOST_PROPERTY_NAME, BrokerConstants.HOST);

                config.put(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
                config.put(IConfig.AUTHENTICATOR_CLASS_NAME, "");

                config.put(IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME, Boolean.FALSE.toString());
            }

            try {
                server.startServer(config);

                Log.i(TAG, "Started MQTT broker on port " + server.getPort());
            } catch (Exception e) {
                Log.e(TAG, "Problem starting MQTT broker", e);
            }
        }
    }


    private void stopServer() {
        if(started) {
            started = false;
            server.stopServer();
        }
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    public Server getServer() {
        return server;
    }
}