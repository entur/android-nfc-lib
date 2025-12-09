package no.entur.android.nfc.external.mqtt.test;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class WebSocketNfcServer extends WebSocketServer {

    private static final String LOG_TAG = WebSocketNfcServer.class.getName();

    public WebSocketNfcServer(int port) {
        super(new InetSocketAddress(port));
    }

    public WebSocketNfcServer(InetSocketAddress address) {
        super(address);
    }

    public WebSocketNfcServer(int port, Draft_6455 draft) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(LOG_TAG, "onOpen");

        // https://github.com/moquette-io/moquette/blob/20f41ef2e34e1baf8c9cc4880d5cbad0e2ad0e38/broker/src/test/java/io/moquette/integration/MQTTWebSocket.java#L34
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(LOG_TAG, "onClose");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(LOG_TAG, conn + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(LOG_TAG, conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(LOG_TAG, "Error", ex);
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "Server started!");
    }

}