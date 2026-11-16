package no.entur.android.nfc.websocket.client;

import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;

public class DefaultWebSocketReader extends WebSocketNfcMessageReader {

        private OkHttpClient client;
        private WebSocketClientListener listener;

        public DefaultWebSocketReader(NfcMessageListener delegate, OkHttpClient client) {
            super(delegate);
            this.client = client;
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
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();

        if(listener != null) {
            listener.onClosed();
        }
    }
}
