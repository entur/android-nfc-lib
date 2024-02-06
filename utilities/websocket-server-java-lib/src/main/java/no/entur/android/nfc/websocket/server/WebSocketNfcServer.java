package no.entur.android.nfc.websocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import no.entur.android.nfc.websocket.messages.card.CardServer;
import no.entur.android.nfc.websocket.messages.reader.ReaderServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class WebSocketNfcServer extends WebSocketServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcServer.class);

    private final NfcMessageReader reader = new NfcMessageReader();
    private final CardTerminalsFilter cardTerminalsFilter;

    private CardTerminalsPollingPool cardTerminalsPollingPool = new CardTerminalsPollingPool();

    private CardTerminalsPollingServer cardTerminalsPollingServer;

    private class Attachment implements CardListener, CardServer.Listener, ReaderServer.Listener {

        private CardServer cardServer;

        private ReaderServer readerServer;

        private CompositeNfcMessageListener compositeNfcMessageListener;

        private PooledCardTerminal pooledCardTerminal;

        private Card card;
        private CardChannel cardChannel;

        @Override
        public void cardConnected(Card card) {
            this.card = card;

            this.cardChannel = card.getBasicChannel();

            cardServer.cardPresent(Arrays.asList());
        }

        @Override
        public void cardDisconnected(Card card) {
            this.card = null;
            this.cardChannel = null;

            cardServer.cardLost();
        }

        public byte[] transcieve(byte[] command) throws IOException {

            ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

            try {
                int count = cardChannel.transmit(ByteBuffer.wrap(command), outputBuffer);

                byte[] response = new byte[count];
                System.arraycopy(outputBuffer.array(), 0, response , 0, response .length);

                return response;
            } catch (CardException e) {
                throw new IOException(e);
            }
        }

        @Override
        public boolean onConnect() {
            LOGGER.info("on connect reader");

            PooledCardTerminal borrow = cardTerminalsPollingPool.borrow();
            if(borrow != null) {
                this.pooledCardTerminal = borrow;

                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean onDisconnect() {
            LOGGER.info("on disconnect reader");

            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                cardTerminalsPollingPool.unborrow(pooledCardTerminal);
                this.pooledCardTerminal = null;
            }

            return true;
        }

        @Override
        public boolean onBeginPolling() {
            LOGGER.info("on begin polling");

            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                pooledCardTerminal.startPolling();
                return true;
            }
            return false;
        }

        @Override
        public boolean onEndPolling() {
            LOGGER.info("on end polling");
            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                pooledCardTerminal.stopPolling();
                return true;
            }
            return false;
        }
    }

    public WebSocketNfcServer(int port, CardTerminalsFilter cardTerminalsFilter) throws UnknownHostException {
        super(new InetSocketAddress(port));

        this.cardTerminalsFilter = cardTerminalsFilter;

        //TerminalManager.fixPlatformPaths();

        //System.setProperty(TerminalManager.LIB_PROP, "/usr/lib64/libpcsclite.so.1.0.0");
    }

    public WebSocketNfcServer(InetSocketAddress address, CardTerminalsFilter cardTerminalsFilter) {
        super(address);
        this.cardTerminalsFilter = cardTerminalsFilter;
    }

    public WebSocketNfcServer(int port, Draft_6455 draft, CardTerminalsFilter cardTerminalsFilter) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
        this.cardTerminalsFilter = cardTerminalsFilter;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info("onOpen");

        Attachment attachment = new Attachment();
        WebSocketNfcMessageWriter writer = new WebSocketNfcMessageWriter(conn);
        attachment.cardServer = new CardServer(writer);
        attachment.readerServer = new ReaderServer(writer);

        attachment.compositeNfcMessageListener = new CompositeNfcMessageListener();
        attachment.compositeNfcMessageListener.add(attachment.cardServer);
        attachment.compositeNfcMessageListener.add(attachment.readerServer);

        attachment.cardServer.setListener(attachment);

        attachment.readerServer.setListener(attachment);

        conn.setAttachment(attachment);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("onClose");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(conn + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        NfcMessage m = reader.parse(message.array());
        if (m != null) {
            Attachment attachment = conn.getAttachment();

            attachment.compositeNfcMessageListener.onMessage(m);
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("Error", ex);
    }

    @Override
    public void onStart() {
        LOGGER.info("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);

        TerminalFactory f = TerminalFactory.getDefault();

        cardTerminalsPollingServer = new CardTerminalsPollingServer(f, cardTerminalsPollingPool);
        cardTerminalsPollingServer.setCardTerminalsFilter(cardTerminalsFilter);
        cardTerminalsPollingServer.start();
    }



}