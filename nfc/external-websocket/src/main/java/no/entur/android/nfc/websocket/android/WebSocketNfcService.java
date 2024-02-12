package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;
import no.entur.android.nfc.websocket.client.WebSocketClientListener;
import no.entur.android.nfc.websocket.messages.card.CardClient;

public class WebSocketNfcService extends Service implements CardClient.Listener, WebSocketClientListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    protected INFcTagBinder infcTagBinder;

    //binder given to client
    private final IBinder binder = new LocalBinder();

    private WebSocketClientFactory factory = new WebSocketClientFactory(2000, 1000);

    private IsoDepTagServiceSupport isoDepTagServiceSupport = new IsoDepTagServiceSupport(this, new INFcTagBinder(store), store);
    private WebsocketMifareUltralightTagServiceSupport mifareUltralightTagServiceSupport = new WebsocketMifareUltralightTagServiceSupport(this, new INFcTagBinder(store), store);

    private WebSocketClient client = null;

    public class LocalBinder extends Binder {
        public boolean connect(String uri) {
            return WebSocketNfcService.this.connect(uri);
        }

        public boolean connectReader(String[] tags) {
            return WebSocketNfcService.this.connectReader(tags);
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

    public boolean connectReader(String[] tags) {
        WebSocketClient c = this.client;
        if(c != null) {
            if(c.getReaderClient().connect(tags)) {
                broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);
            } else {
                LOGGER.warn("Unable to connect reader");
            }
        }
        return false;
    }

    public boolean disconnectReader() {
        WebSocketClient c = this.client;
        if(c != null) {
            if(c.getReaderClient().disconnect()) {
                broadcast(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
            } else {
                LOGGER.warn("Unable to disconnect reader");
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

        if(technologies.contains("IsoDep")) {
            WebsocketIsoDepWrapper wrapper = new WebsocketIsoDepWrapper(cardClient);

            isoDepTagServiceSupport.card(-1, wrapper, uid, historicalBytes, IntentEnricher.identity());
        } else if(technologies.contains("MifareUltralight")) {
            WebsocketIsoDepWrapper wrapper = new WebsocketIsoDepWrapper(cardClient);

            mifareUltralightTagServiceSupport.mifareUltralight(-1, wrapper, atr, uid, historicalBytes, IntentEnricher.identity());
        } else {

            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
    }

    public void broadcast(String action) {
        LOGGER.info("Broadcast " + action);
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

}
