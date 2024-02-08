package no.entur.android.nfc.websocket.client;

import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClientFactory {


    public WebSocketClient connect(String url, WebSocketClientListener listener) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        CompositeNfcMessageListener local = new CompositeNfcMessageListener();

        WebsocketNfcMessageWriter sender = new WebsocketNfcMessageWriter();

        Request request = new Request.Builder().url(url).build();

        RequestResponseMessages requestResponseMessages = new RequestResponseMessages(local, sender);

        DefaultWebSocketReader reader = new DefaultWebSocketReader(requestResponseMessages, client);

        reader.setListener(listener);

        WebSocket webSocket = client.newWebSocket(request, reader);
        sender.setWebSocket(webSocket);

        ReaderClient readerClient = new ReaderClient(requestResponseMessages, 1000);
        CardClient cardClient = new CardClient(requestResponseMessages, 1000);

        local.add(readerClient);
        local.add(cardClient);

        WebSocketClient c = new WebSocketClient();
        c.setWebSocket(webSocket);
        c.setReaderClient(readerClient);
        c.setCardClient(cardClient);
        c.setClosable(requestResponseMessages);

        return c;
    }

}
