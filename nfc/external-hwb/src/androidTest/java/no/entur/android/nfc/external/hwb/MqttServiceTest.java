package no.entur.android.nfc.external.hwb;

import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;

import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import no.entur.android.nfc.external.mqtt.test.broker.Mqtt3Extension;
import no.entur.android.nfc.external.mqtt.test.broker.Mqtt3WebSocketBroker;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MqttServiceTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttServiceTest.class);

    @Test
    public void connectTest() throws Exception {
        int port = 1894;

        List<IProtocol> mqtt = Collections.<IProtocol>singletonList(new Protocol("mqtt"));

        List<IExtension> extensions = Collections.<IExtension>singletonList(new Mqtt3Extension());
        Draft_6455 draft6455 = new Draft_6455(extensions, mqtt);

        Mqtt3WebSocketBroker server = new Mqtt3WebSocketBroker(port, draft6455);
        server.start();
        Thread.sleep(1000);
        System.out.println(server.getAddress());
        try {
            Mqtt3BlockingClient mqtt3BlockingClient = Mqtt3Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .webSocketWithDefaultConfig()
                    .serverAddress(server.getAddress())
                    .buildBlocking();

            Mqtt3ConnAck connect = mqtt3BlockingClient.connect();

            System.out.println(connect);

            Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder().topicFilter("/myTopic").build();
            mqtt3BlockingClient.subscribe(subscribe);

            System.out.println("Subscribe finished");

            Mqtt3Publish publish = Mqtt3Publish.builder().topic("/someTopic").qos(MqttQos.EXACTLY_ONCE).payload("{}".getBytes(StandardCharsets.UTF_8)).build();
            mqtt3BlockingClient.publish(publish);

            System.out.println("Publish finished");

            Mqtt3AsyncClient async = mqtt3BlockingClient.toAsync();





            Thread.sleep(1000);
        } finally {
            server.stop();
        }


    }

    @Test
    public void connect() throws Exception {
        int port = 1895;

        List<IProtocol> mqtt = Collections.<IProtocol>singletonList(new Protocol("mqtt"));

        List<IExtension> extensions = Collections.<IExtension>singletonList(new Mqtt3Extension());
        Draft_6455 draft6455 = new Draft_6455(extensions, mqtt);

        Mqtt3WebSocketBroker server = new Mqtt3WebSocketBroker(port, draft6455);
        server.start();
        Thread.sleep(1000);
        System.out.println(server.getAddress());
        try {
            Mqtt3AsyncClient mqtt3AsyncClient = Mqtt3Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .webSocketWithDefaultConfig()
                    .serverAddress(server.getAddress())
                    .buildAsync();

            Mqtt3ConnAck connect = mqtt3AsyncClient.connect().get();

            System.out.println(connect);

            mqtt3AsyncClient.subscribeWith().topicFilter("/myTopic").callback((a) -> {
                byte[] payloadAsBytes = a.getPayloadAsBytes();
                System.out.println("Got message " + new String(payloadAsBytes));
            }).send().get();

            System.out.println("Subscribe finished");

            Thread.sleep(1000);

            // https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718058
            String topic = "/myTopic";

            byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
            byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);

            ByteBuf buffer = Unpooled.buffer(bytes.length + topicBytes.length + 5 + 2 + 2);

            buffer.writeByte(0b00111100);
            MqttVariableByteInteger.encode(bytes.length + 2 + 2 + topicBytes.length, buffer);
            buffer.writeShort(topicBytes.length);
            buffer.writeBytes(topicBytes);

            buffer.writeByte(0x01);
            buffer.writeByte(0x01);

            buffer.writeBytes(bytes);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            bout.write(buffer.array(), 0, buffer.readableBytes());

            server.publishRaw(topic, bout.toByteArray());


            server.publish(topic,  2, "{adadsfs}".getBytes(StandardCharsets.UTF_8));

            Thread.sleep(1000);
        } finally {
            server.stop();
        }


    }

}
