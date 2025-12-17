package no.entur.android.nfc.external.hwb.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import no.entur.android.nfc.external.mqtt.test.MqttServiceClient;

public class HwbMqttServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HwbMqttServiceClient.class);

    private MqttServiceClient client;

    public HwbMqttServiceClient(MqttServiceClient client) {
        this.client = client;
    }


}
