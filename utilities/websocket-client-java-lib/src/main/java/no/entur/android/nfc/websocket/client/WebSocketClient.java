package no.entur.android.nfc.websocket.client;

import java.io.Closeable;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;
import okhttp3.WebSocket;

public class WebSocketClient {

    private ReaderClient readerClient;
    private CardClient cardClient;

    private WebSocket webSocket;

    private Closeable closeable;

    public ReaderClient getReaderClient() {
        return readerClient;
    }

    public void setReaderClient(ReaderClient readerClient) {
        this.readerClient = readerClient;
    }

    public CardClient getCardClient() {
        return cardClient;
    }

    public void setCardClient(CardClient cardClient) {
        this.cardClient = cardClient;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public void close() throws IOException {
        try {
            webSocket.close(1000, null);
        } finally {
            closeable.close();
        }
    }

    public void setClosable(RequestResponseMessages closeable) {
        this.closeable = closeable;
    }
}
