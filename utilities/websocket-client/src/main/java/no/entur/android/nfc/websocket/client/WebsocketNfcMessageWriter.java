package no.entur.android.nfc.websocket.client;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageWriter;
import okhttp3.WebSocket;
import okio.ByteString;

public class WebsocketNfcMessageWriter implements NfcMessageListener  {

    private final NfcMessageWriter writer = new NfcMessageWriter();

    private WebSocket webSocket;

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void onMessage(NfcMessage message) {
        byte[] bytes = writer.write(message);

        webSocket.send(ByteString.of(bytes));
    }
}
