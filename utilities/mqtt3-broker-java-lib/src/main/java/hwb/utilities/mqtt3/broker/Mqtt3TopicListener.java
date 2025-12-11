package hwb.utilities.mqtt3.broker;

import org.java_websocket.WebSocket;

public interface Mqtt3TopicListener {

     void onPublish(Mqtt3WebSocketBroker broker, WebSocket source, String topic, byte[] bytes);

}
