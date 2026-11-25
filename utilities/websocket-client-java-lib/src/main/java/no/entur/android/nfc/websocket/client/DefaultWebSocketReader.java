package no.entur.android.nfc.websocket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;

public class DefaultWebSocketReader extends WebSocketNfcMessageReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultWebSocketReader.class);

    private OkHttpClient client;
    private WebSocketClientListener listener;

    // i.e. the request-response messages, so that requests waiting for a response return when the web socket is closed
    private final Closeable closeable;

    public DefaultWebSocketReader(NfcMessageListener delegate, OkHttpClient client) {
        this(delegate, client, null);
    }

    public DefaultWebSocketReader(NfcMessageListener delegate, OkHttpClient client, Closeable closeable) {
        super(delegate);
        this.client = client;
        this.closeable = closeable;
    }

    public void setListener(WebSocketClientListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);

        closed();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);

        // the web socket is closed after a failure (onClosed is not invoked), so clean up the same way
        closed();
    }

    private void closed() {
        // no responses will arrive
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.warn("Problem closing", e);
            }
        }

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();

        if(listener != null) {
            listener.onClosed();
        }
    }
}
