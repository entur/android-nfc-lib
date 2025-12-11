package hwb.utilities.mqtt3.broker;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ServerSocketFactory;

/**
 * A simple WebSocketServer MQTT broker implementation for use with testing.
 *
 * While crude, this avoids pulling in netty as a dependency.
 */
public class Mqtt3WebSocketBroker extends WebSocketServer {

    // see https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html

    private static final Logger LOGGER = LoggerFactory.getLogger(Mqtt3WebSocketBroker.class);

    private static final int CONTINUATION_BIT_MASK = 0x80;
    private static final int VALUE_MASK = 0x7f;
    private static final byte VALUE_BITS = 7;

    private static final AtomicInteger messageNumber = new AtomicInteger();

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Random random = new Random(System.nanoTime());

        private int port = -1;

        private List<Mqtt3TopicListener> listeners = new ArrayList<>();

        public Builder withListener(Mqtt3TopicListener listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder withListeners(List<Mqtt3TopicListener> listeners) {
            this.listeners = listeners;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Mqtt3WebSocketBroker build() {
            if(port == -1) {
                port = findAvailablePort(10000, 30000);
            }

            List<IProtocol> mqtt = Collections.<IProtocol>singletonList(new Protocol("mqtt"));

            List<IExtension> extensions = Collections.<IExtension>singletonList(new Mqtt3Extension());
            Draft_6455 draft6455 = new Draft_6455(extensions, mqtt);

            return new Mqtt3WebSocketBroker(port, draft6455, listeners);
        }

        private int findAvailablePort(int minPort, int maxPort) {
            int portRange = maxPort - minPort;

            int randomOffset = random.nextInt(portRange + 1);

            for(int i = minPort; i < maxPort; i++) {

                int port = (i + randomOffset) % portRange;
                if(isPortAvailable(port)) {
                    return port;
                }
            }

            throw new IllegalStateException("Could not find an available port");
        }

        protected boolean isPortAvailable(int port) {
            try {
                ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
                        port, 1, InetAddress.getByName("localhost"));
                serverSocket.close();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public static class Subscription {

        private int qos;
        private String topic;

        public Subscription(String topic, byte qos) {
            this.topic = topic;
            this.qos = qos;
        }
    }

    public class Subscriptions {
        private List<Subscription> subscriptions = new ArrayList<>();

        public void add(String topic, byte qos) {
            subscriptions.add(new Subscription(topic, qos));
        }

        public void remove(String topic) {
            for(int i = 0; i < subscriptions.size(); i++) {
                Subscription subscription = subscriptions.get(i);
                if(subscription.topic.equals(topic)) {
                    subscriptions.remove(i);
                    i--;
                }
            }
        }

        public boolean hasTopic(String topic) {
            for (Subscription subscription : subscriptions) {
                if(subscription.topic.equals(topic)) {
                    return true;
                }
            }
            return false;
        }
    }

    private List<Mqtt3TopicListener> listeners;

    public Mqtt3WebSocketBroker(InetSocketAddress address, List<Mqtt3TopicListener> listeners) {
        super(address);
        this.listeners = listeners;
    }

    public Mqtt3WebSocketBroker(int port, Draft_6455 draft, List<Mqtt3TopicListener> listeners) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
        this.listeners = listeners;
    }

    @Override
    protected boolean onConnect(SelectionKey key) {
        LOGGER.info("onConnect");
        return super.onConnect(key);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info("onOpen");

        Subscriptions subscriptions = new Subscriptions();
        conn.setAttachment(subscriptions);

        // CONNACK
        ByteBuffer msg = ByteBuffer.wrap(new byte[]{0x20, // message type
                0x02, // remaining length 2 bytes
                0x01, // Connect Acknowledge Flags. Bit 0 (SP1) is the Session Present Flag.
                0x00, // Connect Return code. 0x00 Connection Accepted
        });

        conn.send(msg);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("onClose");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(conn + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        int position = message.position();

        message.mark();

        int typeAndFlags = message.get() & 0xFF;

        int type = (typeAndFlags >> 4) & 0xF;
        int flags = typeAndFlags & 0xF;

        int length = decodeRemainingLength(message);

        Mqtt3MessageType mqtt3MessageType = Mqtt3MessageType.fromCode(type);

        switch (mqtt3MessageType) {

            case SUBSCRIBE: {
                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();

                Subscriptions subscriptions = conn.getAttachment();

                while (message.position() < length + 2) {

                    int topicLength = message.getShort();

                    byte[] topicBytes = new byte[topicLength];

                    message.get(topicBytes);

                    byte qos = message.get();

                    String topic = new String(topicBytes);

                    LOGGER.info("Subscribe to " + topic + " with QoS " + String.format("%02X", qos));

                    subscriptions.add(topic, qos);
                }

                // SUBACK
                ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0x90, // message type
                        0x03, // remaining length 3 bytes
                        packetIdentifierMsb, packetIdentifierLsb, // packet identifier
                        0x02, // return code Maximum QoS 2
                });

                conn.send(msg);

                break;
            }
            case UNSUBSCRIBE: {
                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();

                Subscriptions subscriptions = conn.getAttachment();

                while (message.position() < length + 2) {

                    int topicLength = message.getShort() & 0xFFFF;

                    byte[] topicBytes = new byte[topicLength];

                    message.get(topicBytes);

                    String topic = new String(topicBytes);

                    LOGGER.info("Unsubscribe to " + topic);

                    subscriptions.remove(topic);
                }

                // SUBACK
                ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0xB0, // message type
                        0x02, // remaining length 3 bytes
                        packetIdentifierMsb, packetIdentifierLsb, // packet identifier
                });

                conn.send(msg);
                break;
            }
            case PUBLISH: {
                int topicLength = message.getShort() & 0xFFFF;

                byte[] topicBytes = new byte[topicLength];

                message.get(topicBytes);

                String topic = new String(topicBytes);

                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();

                int payloadLength = message.position() - length - 2;

                byte[] payload = new byte[payloadLength];
                message.get(payload);

                int qos = (flags >>> 1) & 0x3;

                LOGGER.info("Publish to " + new String(topicBytes) + " with QoS " + String.format("%02X", qos) + ":\n" + new String(payload));

                if (qos == 2) {
                    // PUBREC
                    ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0x50, // message type
                            0x02, // remaining length 2 bytes
                            packetIdentifierMsb, packetIdentifierLsb, // packet identifier
                    });
                    conn.send(msg);
                } else if (qos == 1) {
                    // PUBACK
                    ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0x40, // message type
                            0x02, // remaining length 2 bytes
                            packetIdentifierMsb, packetIdentifierLsb, // packet identifier
                    });
                    conn.send(msg);
                } else if (qos == 0) {
                    // do nothing
                }

                int read = message.position() - position;

                message.reset();

                byte[] bytes = new byte[read];
                message.get(bytes);

                // clear dup flag
                bytes[0] = (byte) (bytes[0] & ~0b11110111);

                publishToClients(conn, topic, bytes);

                for (Mqtt3TopicListener listener : listeners) {
                    listener.onPublish(this, conn, topic, bytes);
                }

                break;
            }
            case PUBREL: {
                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();

                // PUBCOMP
                ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0x70, // message type
                        0x02, // remaining length 2 bytes
                        packetIdentifierMsb, packetIdentifierLsb, // packet identifier
                });
                conn.send(msg);

                break;
            }
            case PINGREQ: {
                // PINGRESP
                ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0xD0, // message type
                        0x00, // remaining length 0 bytes
                });
                conn.send(msg);
                break;
            }
            case PUBREC: { // client sends this message when we publish
                // PUBREL
                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();

                ByteBuffer msg = ByteBuffer.wrap(new byte[]{(byte) 0x70, // message type
                        0x02, // remaining length 0 bytes
                        packetIdentifierMsb, packetIdentifierLsb
                });
                conn.send(msg);
                break;
            }
            case PUBACK: { // client sends this message when we publish - QoS level 1
                byte packetIdentifierMsb = message.get();
                byte packetIdentifierLsb = message.get();
                break;
            }
        }
    }

    public void publishToClients(WebSocket source, String topic, byte[] bytes) {
        for (WebSocket connection : getConnections()) {
            if(connection == source) {
                continue;
            }
            Subscriptions subscriptions = connection.getAttachment();

            if (subscriptions.hasTopic(topic)) {
                connection.send(bytes);
            }
        }
    }

    public void publish(String topic, int qos, byte[] payload) throws IOException {
        byte[] topicBytes = topic.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream bout = new ByteArrayOutputStream(payload.length + topicBytes.length + 5 + 2 + 2);
        DataOutputStream dout = new DataOutputStream(bout);

        int header = 0b00111000 | encodeQos(qos) << 1;

        dout.write(header);
        encode(payload.length + 2 + 2 + topicBytes.length, dout);
        dout.writeShort(topicBytes.length);
        dout.write(topicBytes);

        dout.writeShort(messageNumber.incrementAndGet());

        dout.write(payload);

        publishRaw(topic, bout.toByteArray());
    }

    /**
     * Encodes the given value as a variable byte integer to the given byte buffer at the current writer index.
     * <p>
     * This method does not check if the value is in range of a 4 byte variable byte integer.
     *
     * @param value   the value to encode.
     * @param byteBuf the byte buffer to encode to.
     */

    private void encode(int value, final DataOutput byteBuf) throws IOException {
        do {
            int encodedByte = value & VALUE_MASK;
            value >>>= VALUE_BITS;
            if (value > 0) {
                encodedByte |= CONTINUATION_BIT_MASK;
            }
            byteBuf.writeByte(encodedByte);
        } while (value > 0);
    }


    private int encodeQos(int qos) { // bit 1 and 2
        switch (qos) {
            case 0: return 0;
            case 1: return 1;
            case 2: return 2;
        }
        throw new RuntimeException("Unexpected QoS " + qos);
    }

    public void publishRaw(String topic, byte[] bytes) {
        for(WebSocket connection : getConnections()) {
            Subscriptions subscriptions = connection.getAttachment();

            if(subscriptions.hasTopic(topic)) {
                connection.send(bytes);

                LOGGER.info("Publish message size " + bytes.length + " to topic " + topic);
            }
        }
    }

    private int decodeRemainingLength(ByteBuffer in) {
        int multiplier = 1;
        int value = 0;
        int encodedByte;

        int count = 0;

        do {
            encodedByte = in.get() & 0xFF;

            value += (encodedByte & ~0x80) * multiplier;
            multiplier *= 128;

            if((encodedByte & 0x80) == 0) {
                break;
            }
            count++;
        } while (count < 4); // Continue if the MSB is set

        return value;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("onError", ex);
    }

    @Override
    public void onStart() {
        LOGGER.info("onStart");
    }

    public void clearListeners() {
        this.listeners = new ArrayList<>();
    }

    public void addListener(Mqtt3TopicListener listener) {
        ArrayList<Mqtt3TopicListener> mqtt3TopicListeners = new ArrayList<>(this.listeners);
        mqtt3TopicListeners.add(listener);
        this.listeners = mqtt3TopicListeners;
    }

}