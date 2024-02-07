package no.entur.android.nfc.websocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
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

import jnasmartcardio.Smartcardio;
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
            LOGGER.info("Card connected");
            this.card = card;

            this.cardChannel = card.getBasicChannel();

            cardServer.cardPresent(Arrays.asList());
        }

        @Override
        public void cardDisconnected(Card card) {
            LOGGER.info("Card disconnected");
            this.card = null;
            this.cardChannel = null;

            cardServer.cardLost();
        }

        public byte[] transcieve(byte[] command) throws IOException, CardException {

            ByteBuffer outputBuffer = ByteBuffer.allocate(1024);

            card.beginExclusive();
            try {
                int count = cardChannel.transmit(ByteBuffer.wrap(command), outputBuffer);

                byte[] response = new byte[count];
                System.arraycopy(outputBuffer.array(), 0, response , 0, response .length);

                return response;
            } catch (CardException e) {
                throw new IOException(e);
            } finally {
                card.endExclusive();
            }
        }

        @Override
        public boolean onConnect() {
            PooledCardTerminal borrow = cardTerminalsPollingPool.borrow();
            if(borrow != null) {
                LOGGER.info("Connected reader " + borrow.getCardTerminal().getName() );
                this.pooledCardTerminal = borrow;

                return true;
            }
            LOGGER.info("Unable to connect reader");

            return false;
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
        public boolean onBeginPolling() throws CardException {
            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                LOGGER.info("Begin polling " + pooledCardTerminal.getCardTerminal().getName());
                pooledCardTerminal.setListener(this);
                pooledCardTerminal.startPolling();
                return true;
            }
            LOGGER.info("Cannot begin polling, no reader");

            return false;
        }

        @Override
        public boolean onEndPolling() throws CardException {

            Card card = this.card;
            if(card != null) {
                LOGGER.info("Disconnect card");
                try {
                    card.beginExclusive();
                    card.disconnect(false);
                    card.endExclusive();
                    LOGGER.info("Disconnected card");
                } catch(Exception e) {
                    LOGGER.info("Disconnected card", e);
                } finally {
                    this.card = null;
                }
            }

            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                LOGGER.info("End polling for " + pooledCardTerminal.getCardTerminal().getName());
                pooledCardTerminal.stopPolling();
                pooledCardTerminal.setListener(null);

                LOGGER.info("Ended polling for " + pooledCardTerminal.getCardTerminal().getName());

                return true;
            }
            LOGGER.info("Cannot end polling, no reader or closed");
            return false;
        }
    }

    public WebSocketNfcServer(int port, CardTerminalsFilter cardTerminalsFilter) throws UnknownHostException {
        super(new InetSocketAddress(port));

        this.cardTerminalsFilter = cardTerminalsFilter;
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

        Attachment attachment = conn.getAttachment();
        if(attachment != null) {

            try {
                attachment.onEndPolling();
            } catch (CardException e) {
                LOGGER.info("Problem ending polling", e);
            }
            attachment.onDisconnect();
        }
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

        //TerminalFactory f = TerminalFactory.getDefault();

        TerminalFactory f = null;
        try {
            f = TerminalFactory.getInstance("PC/SC", null, new Smartcardio());
            cardTerminalsPollingServer = new CardTerminalsPollingServer(f, cardTerminalsPollingPool);
            cardTerminalsPollingServer.setCardTerminalsFilter(cardTerminalsFilter);
            cardTerminalsPollingServer.start();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }



}