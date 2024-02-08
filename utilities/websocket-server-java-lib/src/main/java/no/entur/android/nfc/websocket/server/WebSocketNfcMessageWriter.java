package no.entur.android.nfc.websocket.server;

import org.java_websocket.WebSocket;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageWriter;

public class WebSocketNfcMessageWriter implements NfcMessageListener {

    private final NfcMessageWriter writer = new NfcMessageWriter();

    private final WebSocket webSocket;

    public WebSocketNfcMessageWriter(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(NfcMessage message) {
        byte[] bytes = writer.write(message);

        webSocket.send(bytes);
    }
}
