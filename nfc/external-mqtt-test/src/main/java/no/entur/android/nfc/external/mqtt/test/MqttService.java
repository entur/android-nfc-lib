package no.entur.android.nfc.external.mqtt.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MqttService extends Service {

    public static final String EXTRA_CONFIGURATION = MqttService.class.getName() + ".EXTRA_CONFIGURATION";

    private static final String TAG = MqttService.class.getName();

    protected Server server = new Server();
    protected boolean started = false;

    protected final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }

        public Server getServer() {
            return server;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
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