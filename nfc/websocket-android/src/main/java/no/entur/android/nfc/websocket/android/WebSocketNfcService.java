package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;
import no.entur.android.nfc.websocket.client.WebSocketClientListener;
import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class WebSocketNfcService extends Service implements CardClient.Listener, WebSocketClientListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    protected INFcTagBinder infcTagBinder;

    //binder given to client
    private final IBinder binder = new LocalBinder();

    private WebSocketClientFactory factory = new WebSocketClientFactory();

    private IsoDepTagServiceSupport isoDepTagServiceSupport = new IsoDepTagServiceSupport(this, new INFcTagBinder(store), store);

    private WebSocketClient client = null;

    public class LocalBinder extends Binder {
        public boolean connect(String uri) {
            return WebSocketNfcService.this.connect(uri);
        }

        public boolean connectReader() {
            return WebSocketNfcService.this.connectReader();
        }

        public boolean disconnectReader() {
            return WebSocketNfcService.this.disconnectReader();
        }

        public void disconnect() throws IOException {
            WebSocketNfcService.this.disconnect();
        }

        public boolean beginPolling() {
            return WebSocketNfcService.this.beginPolling();
        }

        public void endPolling() throws IOException {
            WebSocketNfcService.this.endPolling();
        }


    }

    @Override
    public void onCreate() {
        super.onCreate();

        LOGGER.info("onCreate");

        this.infcTagBinder = new INFcTagBinder(store);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean connect(String uri) {
        try {
            LOGGER.warn("Connect to " + uri);
            client = factory.connect(uri, this);

            broadcast(ExternalNfcServiceCallback.ACTION_SERVICE_STARTED);
        } catch(Exception e) {
            LOGGER.warn("Problem connecting to " + uri);
            return false;
        }
        return true;
    }

    public boolean connectReader() {
        WebSocketClient c = this.client;
        if(c != null) {
            if(c.getReaderClient().connect()) {
                broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);
            }
        }
        return false;
    }

    public boolean disconnectReader() {
        WebSocketClient c = this.client;
        if(c != null) {
            if(c.getReaderClient().disconnect()) {
                broadcast(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
            }
        }
        return false;
    }

    public void disconnect() throws IOException {
        WebSocketClient c = this.client;
        if(c != null) {
            c.close();
        }
        broadcast(ExternalNfcServiceCallback.ACTION_SERVICE_STOPPED);

    }

    public boolean beginPolling() {
        WebSocketClient c = this.client;
        if(c != null) {
            c.getCardClient().setListener(this);

            return c.getReaderClient().beginPolling();
        }
        return false;
    }

    public void endPolling() throws IOException {
        WebSocketClient c = this.client;
        if(c != null) {
            c.getReaderClient().endPolling();
            c.getCardClient().setListener(null);
        }
    }

    @Override
    public void onClosed() {
        this.client = null;
    }

    @Override
    public void onCardLost() {
        LOGGER.info("onCardLost");

        broadcast(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);
    }

    @Override
    public void onCardPresent(CardClient cardClient, List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid) {
        LOGGER.info("onCardPresent: " + technologies);

        WebsocketIsoDepWrapper wrapper = new WebsocketIsoDepWrapper(cardClient);

        isoDepTagServiceSupport.card(-1, wrapper, uid, historicalBytes, IntentEnricher.identity());
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

}
