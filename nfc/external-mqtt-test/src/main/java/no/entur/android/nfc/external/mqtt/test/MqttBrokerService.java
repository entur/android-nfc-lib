package no.entur.android.nfc.external.mqtt.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import no.entur.android.nfc.external.mqtt3.broker.Mqtt3WebSocketBroker;

public class MqttBrokerService extends Service {

    private static final String LOG_TAG = MqttBrokerService.class.getName();

    public static final String EXTRA_CONFIGURATION = MqttBrokerService.class.getName() + ".EXTRA_CONFIGURATION";

    public static final String PORT = MqttBrokerService.class.getName() + ".PORT";

    private static final String TAG = MqttBrokerService.class.getName();

    protected Mqtt3WebSocketBroker broker;
    protected boolean started = false;

    protected final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MqttBrokerService getService() {
            return MqttBrokerService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting " + MqttBrokerService.class.getName() + " service");

        createBroker(intent);
        return START_STICKY;
    }

    public void startBroker() {
        if(!started) {
            try {
                broker.start();

                started = true;

                Log.i(TAG, "Started MQTT broker on port " + broker.getPort());
            } catch (Exception e) {
                Log.e(TAG, "Problem starting MQTT broker", e);

                started = false;

                throw e;
            }
        }
    }

    private void createBroker(Intent intent) {
        Properties config = new Properties();
        if(intent.hasExtra(EXTRA_CONFIGURATION)) {
            Map<String, String> map = (HashMap) intent.getSerializableExtra(EXTRA_CONFIGURATION);
            config.putAll(map);
        }

        int port = getPort(config);

        if(broker != null) {
            if(broker.getPort() != port) {
                throw new IllegalStateException("Broker already configured on port " + broker.getPort() + ", please restart service to reset.");
            }
        }

        if(broker == null) {
            Mqtt3WebSocketBroker.Builder builder = Mqtt3WebSocketBroker.newBuilder();
            builder.withPort(port);

            broker = builder.build();
        }
    }

    private static int getPort(Properties config) {
        String property = config.getProperty(MqttBrokerService.PORT);
        int port;
        if(property != null) {
            port = Integer.parseInt(property);
        } else {
            port = -1;
        }
        return port;
    }

    public void stopBroker() throws InterruptedException {
        Log.i(TAG, "Stop the broker on port " + broker.getPort());

        if(started) {
            started = false;

            if(broker != null) {
                Log.i(TAG, "Stop broker");
                broker.stop();
            } else {
                Log.i(TAG, "No broker");
            }
        } else {
            Log.i(TAG, "Broker not started " + this);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        try {
            stopBroker();
        } catch (Exception e) {
            // ignore
            Log.d(LOG_TAG, "Problem stopping broker on port " + broker.getPort());
        }
        super.onDestroy();
    }

    public Mqtt3WebSocketBroker getBroker() {
        return broker;
    }


}