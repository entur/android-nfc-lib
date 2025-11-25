package no.entur.android.nfc.external.hwb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import hwb.utilities.device.diagnostics.DiagnosticsSchema;
import hwb.utilities.validators.nfc.NfcSchema;
import no.entur.android.nfc.external.hwb.reader.HwbReaderService;

public class HwbService implements MqttClientDisconnectedListener {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbService.class);

    private Mqtt3AsyncClient client;

    private Executor executor;

    private long timeout = 1000;

    private Map<String, HwbReaderService> readers;

    private ObjectMapper objectMapper;

    public void addReader(String deviceId) {
        // subscribes to topics
        // /device/[deviceId]/diagnostics <- private topic
        // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

        CompletableFuture<@NotNull Mqtt3ConnAck> connect = client.connect();

        try {
            Mqtt3ConnAck ack = connect.get(timeout, TimeUnit.MILLISECONDS);
            if(ack.getReturnCode() != Mqtt3ConnAckReturnCode.SUCCESS) {
                LOGGER.warn("Unable to add reader {}, connection returned status {}", deviceId, ack.getReturnCode());
                return;
            }


            LOGGER.info("Added reader {}", deviceId);

        } catch (Exception e) {
            LOGGER.error("Problem adding reader {}", deviceId);
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

        client.subscribeWith()
                .topicFilter("/device/diagnostics")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(this::diagnostics)
                .executor(executor)
                .send();
    }

    protected void diagnostics(Mqtt3Publish publish) {

        // is this an NFC-capable device?
        try {
            byte[] payloadAsBytes = publish.getPayloadAsBytes();
            DiagnosticsSchema receiveSchema = objectMapper.readValue(payloadAsBytes, DiagnosticsSchema.class);
            if(readers.containsKey(receiveSchema.getDeviceId())) {
                List<Object> functionality = receiveSchema.getFunctionality();
                if(functionality != null && functionality.contains("nfc")) {
                    addReader(receiveSchema.getDeviceId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Problem parsing ADPU MQTT message", e);
        }


    }

    private void newCard(Mqtt3Publish publish) {
        byte[] payloadAsBytes = publish.getPayloadAsBytes();
        try {
            NfcSchema schema = objectMapper.readValue(payloadAsBytes, NfcSchema.class);


            // do we have this reader? If not then create
            HwbReaderService hwbReaderService = readers.get(schema.getDeviceId());
            if(hwbReaderService == null) {
                hwbReaderService = new HwbReaderService(objectMapper);
                readers.put(schema.getDeviceId(), hwbReaderService);

                // TODO broadcast reader present
                if(hwbReaderService.connect()) {
                    hwbReaderService.subscribe();
                } else {
                    throw new IllegalStateException("Unable to connect");
                }
            }

            // broadcast tag present
            // add one or more metadata fields depending on what is included

            hwbReaderService.newTag(schema);
        } catch (Exception e) {
            LOGGER.error("Problem deserializing message" ,e);
        }
    }


    @Override
    public void onDisconnected(@NotNull MqttClientDisconnectedContext context) {
        // all readers disconnected


    }
}
