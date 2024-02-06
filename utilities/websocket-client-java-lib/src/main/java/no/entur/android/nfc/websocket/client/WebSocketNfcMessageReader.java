package no.entur.android.nfc.websocket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketNfcMessageReader extends WebSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcMessageReader.class);
    private static final String LOG_TAG = WebSocketNfcMessageReader.class.getName();

    private final NfcMessageListener delegate;

    private final NfcMessageReader reader = new NfcMessageReader();

    public WebSocketNfcMessageReader(NfcMessageListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        LOGGER.info("onOpen");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        LOGGER.info("onMessage: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        LOGGER.info("onMessage");

        NfcMessage m = reader.parse(bytes.toByteArray());
        if(m != null) {
            delegate.onMessage(m);
        } else {
            LOGGER.warn("Unable to parse message");
        }

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        LOGGER.info("onClosing");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        LOGGER.info("onFailure: " + response, t);
    }
}
