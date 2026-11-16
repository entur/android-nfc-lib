package no.entur.android.nfc.websocket.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.service.AbstractForegroundService;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.tag.DefaultTransceiveResultExceptionMapper;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;
import no.entur.android.nfc.websocket.client.WebSocketClientListener;
import no.entur.android.nfc.websocket.messages.card.CardClient;

public class WebSocketNfcService extends AbstractForegroundService implements CardClient.Listener, WebSocketClientListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcService.class);

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected WebsocketTagProxyStore store = new WebsocketTagProxyStore();
    // note: the same binder instance must be used for the tags and the reader technology
    protected INFcTagBinder infcTagBinder = new INFcTagBinder(store);

    //binder given to client
    private final IBinder binder = new LocalBinder();

    private WebSocketClientFactory factory = new WebSocketClientFactory(2000, 1000);

    private IsoDepTagServiceSupport isoDepTagServiceSupport = new IsoDepTagServiceSupport(this, infcTagBinder, store, new DefaultTransceiveResultExceptionMapper());
    private WebsocketMifareUltralightTagServiceSupport mifareUltralightTagServiceSupport = new WebsocketMifareUltralightTagServiceSupport(this, infcTagBinder, store, new DefaultTransceiveResultExceptionMapper());

    private volatile WebSocketClient client = null;

    // tag currently in the field, if any
    private volatile TagProxy currentTagProxy = null;

    /**
     * Listener for a single client, so that a late close of a previous client does not clear the current client.
     */

    private class ClientListener implements WebSocketClientListener {

        private WebSocketClient client;
        private boolean closed;

        public synchronized void setClient(WebSocketClient client) {
            this.client = client;
            if(closed) {
                onClientClosed(client);
            }
        }

        @Override
        public synchronized void onClosed() {
            closed = true;
            if(client != null) {
                onClientClosed(client);
            }
        }
    }

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

        this.infcTagBinder.setReaderTechnology(new WebsocketReaderTechnology());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean connect(String uri) {
        try {
            LOGGER.warn("Connect to " + uri);

            // close any previous client, so that it does not linger
            WebSocketClient previous = this.client;
            if(previous != null) {
                onClientClosed(previous);
                try {
                    previous.close();
                } catch(Exception e) {
                    LOGGER.warn("Problem closing previous connection", e);
                }
            }

            ClientListener listener = new ClientListener();
            WebSocketClient c = factory.connect(uri, listener);
            this.client = c;
            listener.setClient(c);

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
            try {
                if (c.getReaderClient().connect(tags)) {
                    broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);

                    return true;
                } else {
                    LOGGER.warn("Unable to connect reader");
                }
            } catch(Exception e) {
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

                return true;
            } else {
                LOGGER.warn("Unable to disconnect reader");
            }
        }
        return false;
    }

    public void disconnect() throws IOException {
        WebSocketClient c = this.client;
        if(c != null) {
            onClientClosed(c);

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

    protected synchronized void onClientClosed(WebSocketClient c) {
        // a previous client might close after a new client was connected
        if(this.client == c) {
            this.client = null;
        }
    }

    @Override
    public void onCardLost() {
        LOGGER.info("onCardLost");

        Intent intent = new Intent();
        intent.setAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);

        TagProxy proxy = this.currentTagProxy;
        if(proxy != null) {
            this.currentTagProxy = null;

            // mark tag as no longer present
            proxy.close();

            byte[] uid = proxy.getUid();
            if(uid != null) {
                intent.putExtra(NfcAdapter.EXTRA_ID, uid);
            }
            intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, proxy.getHandle());
        }

        sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    @Override
    public void onCardPresent(CardClient cardClient, List<String> technologies, byte[] atr, byte[] historicalBytes, byte[] uid) {
        LOGGER.info("onCardPresent: " + technologies);

        // TODO add reader id

        TagProxy previous = this.currentTagProxy;
        if(previous != null) {
            // card lost was not received
            this.currentTagProxy = null;
            previous.close();
        }

        if(technologies.contains("IsoDep")) {
            WebsocketIsoDepWrapper wrapper = new WebsocketIsoDepWrapper(cardClient);

            this.currentTagProxy = isoDepTagServiceSupport.card(-1, wrapper, uid, historicalBytes, IntentEnricher.identity());
        } else if(technologies.contains("MifareUltralight")) {
            WebsocketIsoDepWrapper wrapper = new WebsocketIsoDepWrapper(cardClient);

            TagProxy proxy = mifareUltralightTagServiceSupport.mifareUltralight(-1, wrapper, atr, uid, historicalBytes, IntentEnricher.identity());
            if(proxy != null) {
                proxy.setUid(uid);
            }
            this.currentTagProxy = proxy;
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
