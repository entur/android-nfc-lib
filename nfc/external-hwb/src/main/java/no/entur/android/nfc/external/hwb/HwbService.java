package no.entur.android.nfc.external.hwb;

import android.os.Build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.hwb.reader.HwbReader;
import no.entur.android.nfc.mqtt.messages.JsonResponseMqttMessage;

public class HwbService implements MqttClientDisconnectedListener {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbService.class);

    private Mqtt3AsyncClient client;

    private Executor executor;

    private long timeout = 1000;

    private Map<String, HwbReader> readers;

    private ObjectMapper objectMapper;

    public boolean addReader(String deviceId) {
        // subscribes to topics
        // /device/[deviceId]/diagnostics <- private topic
        // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

        CompletableFuture<@NotNull Mqtt3ConnAck> connect = client.connect();

        try {
            Mqtt3ConnAck ack = connect.get(timeout, TimeUnit.MILLISECONDS);
            if(ack.getReturnCode() != Mqtt3ConnAckReturnCode.SUCCESS) {
                return false;
            }

            CompletableFuture<Mqtt3SubAck> send = client.subscribeWith().topicFilter("/device/" + deviceId + "/diagnostics").send();

        } catch (Exception e) {
            return false;
        }

    }


    public void subscribe() {
        // subscribes to topics
        // /device/[deviceId]/diagnostics <- private topic
        // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

        client.subscribeWith()
                .topicFilter("/validators/nfc")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(this::newCard)
                .executor(executor)
                .send();

    }

    private void newCard(Mqtt3Publish publish) {
        byte[] payloadAsBytes = publish.getPayloadAsBytes();
        try {
            NfcSchema schema = objectMapper.readValue(payloadAsBytes, NfcSchema.class);


            // do we have this reader? If not then create
            HwbReader hwbReader = readers.get(schema.getDeviceId());
            if(hwbReader == null) {
                hwbReader = new HwbReader(objectMapper);
                readers.put(schema.getDeviceId(), hwbReader);

                // TODO broadcast reader present
                if(hwbReader.connect()) {
                    hwbReader.subscribe();
                } else {
                    throw new IllegalStateException("Unable to connect");
                }
            }

            // broadcast tag present
            // add one or more metadata fields depending on what is included

            hwbReader.newTag(schema);
        } catch (Exception e) {
            LOGGER.error("Problem deserializing message" ,e);
        }
    }


}
