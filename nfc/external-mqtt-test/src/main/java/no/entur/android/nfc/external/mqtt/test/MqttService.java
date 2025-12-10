package no.entur.android.nfc.external.mqtt.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import no.entur.android.nfc.external.mqtt.test.broker.SocketUtils;
import no.entur.android.nfc.external.mqtt.test.broker.Mqtt3WebSocketBroker;

public class MqttService extends Service {

    public static final String EXTRA_CONFIGURATION = MqttService.class.getName() + ".EXTRA_CONFIGURATION";

    public static final String PORT = MqttService.class.getName() + ".PORT";

    private static final String TAG = MqttService.class.getName();

    protected Mqtt3WebSocketBroker broker;
    protected boolean started = false;

    protected final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MqttService getService() {
            return MqttService.this;
        }

        public Mqtt3WebSocketBroker getBroker() {
            return broker;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");

        startBroker(intent);
        return START_STICKY;
    }

    private void startBroker(Intent intent) {
        if(!started) {
            started = true;

            Properties config = new Properties();
            if(intent.hasExtra(EXTRA_CONFIGURATION)) {
                Map<String, String> map = (HashMap) intent.getSerializableExtra(EXTRA_CONFIGURATION);
                config.putAll(map);
            }

            try {
                String property = config.getProperty(MqttService.PORT);
                int port;
                if(property != null) {
                    port = Integer.parseInt(property);
                } else {
                    port = SocketUtils.findAvailableTcpPort(10000, 30000);
                }

                broker = new Mqtt3WebSocketBroker(port);
                broker.start();

                Log.i(TAG, "Started MQTT broker on port " + broker.getPort());
            } catch (Exception e) {
                Log.e(TAG, "Problem starting MQTT broker", e);
            }
        }
    }

    private void stopBroker() throws InterruptedException {
        if(started) {
            started = false;
            if(broker != null) {
                broker.stop();
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            stopBroker();
        } catch (InterruptedException e) {
            // ignore
        }
        super.onDestroy();
    }

    public Mqtt3WebSocketBroker getBroker() {
        return broker;
    }
}