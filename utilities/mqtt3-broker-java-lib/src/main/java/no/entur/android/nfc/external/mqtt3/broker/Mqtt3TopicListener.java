package no.entur.android.nfc.external.mqtt3.broker;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.java_websocket.WebSocket;

public interface Mqtt3TopicListener {

     void onPublish(Mqtt3WebSocketBroker broker, WebSocket source, String topic, byte[] bytes);

}
