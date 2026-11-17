package no.entur.android.nfc.websocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import jnasmartcardio.Smartcardio;
import no.entur.android.nfc.websocket.messages.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.messages.CompositeNfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessage;
import no.entur.android.nfc.websocket.messages.NfcMessageListener;
import no.entur.android.nfc.websocket.messages.NfcMessageReader;
import no.entur.android.nfc.websocket.messages.card.CardLostException;
import no.entur.android.nfc.websocket.messages.card.CardServer;
import no.entur.android.nfc.websocket.messages.reader.ReaderServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class WebSocketNfcServer extends WebSocketServer {

    private static final byte[] GET_TAG_ID = new byte[] { (byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00 };

    // the reader detects the card again after a transparent session
    private static final long REACTIVATION_TIMEOUT = 2000;

    private static String toHexString(byte[] bytes) {
        return bytes != null ? ByteArrayHexStringConverter.toHexString(bytes) : "-";
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketNfcServer.class);

    private final NfcMessageReader reader = new NfcMessageReader();
    private final CardTerminalsFilter cardTerminalsFilter;

    private CardTerminalsPollingPool cardTerminalsPollingPool;

    private CardTerminalsPollingServer cardTerminalsPollingServer;

    /**
     * A detected card, from when it is connected until it is removed or polling ends: the card connection (possibly
     * reconnected, replaced as a whole so that the card and channel always match), and what is known about the card.
     */

    private static final class CardConnection {

        // as connected by the polling server, which disconnects it
        private final Card original;

        // the original card, or reconnected after reading the version and signature
        private final Card card;
        private final CardChannel channel;

        private final ATR atr;
        private final List<String> technologies;

        // native MIFARE Ultralight commands must be translated to PC/SC commands
        private final boolean mifareUltralight;
        private final MifareUltralightPcscAdapter mifareUltralightPcscAdapter;

        // DESFire on a reader which appends a status word to single byte (native) responses
        private final boolean stripAppendedStatusWord;

        // empty if the UID could not be read
        private byte[] uid = new byte[0];

        // whether the client was told that this card is lost. Guarded by the attachment lock.
        private boolean lost;

        private CardConnection(Card original, Card card, ATR atr, List<String> technologies, MifareUltralightPcscAdapter mifareUltralightPcscAdapter, boolean stripAppendedStatusWord) {
            this.original = original;
            this.card = card;
            this.channel = card.getBasicChannel();
            this.atr = atr;
            this.technologies = technologies;
            this.mifareUltralight = technologies.contains("MifareUltralight");
            this.mifareUltralightPcscAdapter = mifareUltralightPcscAdapter;
            this.stripAppendedStatusWord = stripAppendedStatusWord;
        }

        private byte[] transcieve(byte[] command) throws IOException, CardException {
            card.beginExclusive();
            try {
                if(mifareUltralight) {
                    // one native command might need several APDUs, so translate within the exclusive session
                    return mifareUltralightPcscAdapter.transceive(command, this::transmit);
                }
                byte[] response = transmit(command);
                if(stripAppendedStatusWord) {
                    response = DesfireResponseAdapter.stripAppendedStatusWord(command, response);
                }
                return response;
            } finally {
                try {
                    card.endExclusive();
                } catch (Exception e) {
                    // i.e. the card was removed; do not hide the response or the original problem
                    LOGGER.debug("Problem ending exclusive access", e);
                }
            }
        }

        private byte[] transmit(byte[] command) throws IOException {
            try {
                // as-is, so that i.e. native DESFire commands and responses are not mangled
                return RawCardChannel.transmit(card, channel, command);
            } catch (CardException e) {
                throw new IOException(e);
            }
        }

        /**
         * Disconnect the card if it was reconnected; the polling server disconnects the original card.
         */

        private void disconnectReconnected() {
            disconnectReconnected(original, card);
        }

        private static void disconnectReconnected(Card original, Card card) {
            if(card != original) {
                try {
                    card.disconnect(false);
                } catch (Exception e) {
                    LOGGER.debug("Problem disconnecting reconnected card", e);
                }
            }
        }
    }

    class Attachment implements CardServer.Listener, ReaderServer.Listener {

        private CardServer cardServer;

        private ReaderServer readerServer;

        private CompositeNfcMessageListener compositeNfcMessageListener;

        private volatile PooledCardTerminal pooledCardTerminal;

        // Guards the current polling session and card, and the card events sent to the client, so that events from
        // a previous polling session or card cannot change or report on the current one.
        private final Object lock = new Object();

        // current polling session, if any. Guarded by lock.
        private PollingSession session;

        // current card, if any. Set by the polling thread (under lock), used by the web socket thread.
        private volatile CardConnection connection;

        /**
         * Card events from polling one reader, from begin polling until end polling (also when connecting to another
         * reader). Events from an ended session, i.e. still in flight when polling ended, are ignored, and cards
         * reconnected for them are disconnected.
         */

        private final class PollingSession implements CardListener {

            private final PooledCardTerminal pooledCardTerminal;

            private PollingSession(PooledCardTerminal pooledCardTerminal) {
                this.pooledCardTerminal = pooledCardTerminal;
            }

            @Override
            public void cardConnected(Card card) throws CardException {
                LOGGER.info("Card connected");

                CardConnection connection = detect(pooledCardTerminal.getCardTerminal(), card);
                try {
                    synchronized (lock) {
                        if(session != this) {
                            LOGGER.info("Polling ended meanwhile, ignore card");

                            connection.disconnectReconnected();

                            return;
                        }
                        Attachment.this.connection = connection;

                        cardServer.cardPresent(connection.technologies, connection.atr.getBytes(), connection.atr.getHistoricalBytes(), connection.uid);
                    }
                } catch (RuntimeException e) {
                    // i.e. the client connection was closed meanwhile. The polling server does not report a card as
                    // disconnected if connecting it failed, so forget the card here.
                    synchronized (lock) {
                        if(Attachment.this.connection == connection) {
                            Attachment.this.connection = null;
                        }
                    }
                    connection.disconnectReconnected();

                    throw e;
                }
            }

            @Override
            public void cardDisconnected(Card card) {
                LOGGER.info("Card disconnected");

                synchronized (lock) {
                    CardConnection current = Attachment.this.connection;
                    if(session != this || current == null || current.original != card) {
                        // i.e. polling ended meanwhile, which released the card
                        LOGGER.info("Ignore card disconnected from previous polling");

                        return;
                    }
                    Attachment.this.connection = null;

                    current.disconnectReconnected();

                    if(!current.lost) {
                        current.lost = true;

                        cardServer.cardLost();
                    } else {
                        LOGGER.info("Card was already reported as lost");
                    }
                }
            }
        }

        /**
         * Find out what the card is. Reading the version and signature re-activates the card, so then it is reconnected.
         *
         * @param cardTerminal reader
         * @param card the card as connected by the polling server
         * @return the card connection
         */

        private CardConnection detect(ExtendedCardTerminal cardTerminal, Card card) throws CardException {
            ATR atr = card.getATR();

            List<String> technologies = new ArrayList<>(cardTerminal.identifyTechnologies(card, card.getBasicChannel()));

            MifareUltralightPcscAdapter mifareUltralightPcscAdapter = new MifareUltralightPcscAdapter();
            Card current = card;
            try {
                if(MifareUltralightPcscAdapter.isMifareUltralightCandidate(atr.getHistoricalBytes())) {
                    Card reconnected = readVersionAndSignature(cardTerminal, mifareUltralightPcscAdapter);
                    if(reconnected != null) {
                        current = reconnected;
                    }

                    if(mifareUltralightPcscAdapter.hasMifareUltralightVersion() && !technologies.contains("MifareUltralight")) {
                        // i.e. NTAG
                        technologies.add("MifareUltralight");
                    }
                }

                LOGGER.info("Got technologies " + technologies);

                boolean stripAppendedStatusWord = DesfireResponseAdapter.isDesfire(atr.getBytes()) && cardTerminal.isStatusWordAppendedToSingleByteResponses();

                CardConnection connection = new CardConnection(card, current, atr, technologies, mifareUltralightPcscAdapter, stripAppendedStatusWord);
                connection.uid = readUid(connection);
                return connection;
            } catch (CardException | RuntimeException e) {
                CardConnection.disconnectReconnected(card, current);

                throw e;
            }
        }

        /**
         * Read GET_VERSION and READ_SIG via the reader, if supported. This re-activates the card,
         * so reconnect afterwards.
         *
         * @return the reconnected card, or null if not re-activated
         */

        private Card readVersionAndSignature(ExtendedCardTerminal cardTerminal, MifareUltralightPcscAdapter mifareUltralightPcscAdapter) throws CardException {
            boolean reactivated;
            try {
                reactivated = mifareUltralightPcscAdapter.readVersionAndSignature(cardTerminal);
            } catch (CardException e) {
                LOGGER.warn("Unable to read version and signature", e);

                // might have been re-activated
                reactivated = true;
            }
            if(!reactivated) {
                return null;
            }
            LOGGER.info("Read version " + toHexString(mifareUltralightPcscAdapter.getVersion()) + " and signature " + toHexString(mifareUltralightPcscAdapter.getSignature()) + ", reconnect");

            if(!cardTerminal.waitForCardPresent(REACTIVATION_TIMEOUT)) {
                throw new CardException("Card not present after re-activation");
            }
            return cardTerminal.connect("*");
        }

        /**
         * @return the UID, or empty if it could not be read
         */

        private byte[] readUid(CardConnection connection) throws CardException {
            connection.card.beginExclusive();
            try {
                ResponseAPDU response = connection.channel.transmit(new CommandAPDU(GET_TAG_ID));
                if (isSuccess(response)) {
                    byte[] uid = response.getData();

                    LOGGER.info("Read UID " + ByteArrayHexStringConverter.toHexString(uid));

                    return uid;
                }
                LOGGER.info("Unable to read UID");
            } catch (CardException e) {
                LOGGER.info("Unable to read UID", e);
            } finally {
                connection.card.endExclusive();
            }
            return new byte[0];
        }

        public boolean isSuccess(ResponseAPDU in) {
            return in.getSW1() == 0x90 && in.getSW2() == 0x00;
        }

        public byte[] transcieve(byte[] command) throws IOException, CardException {
            // the polling thread might replace or clear the connection meanwhile, so use the same one throughout
            CardConnection connection = this.connection;
            if(connection == null) {
                throw new CardException("No card");
            }
            synchronized (lock) {
                if(connection.lost) {
                    throw new CardLostException("Card was lost");
                }
            }
            try {
                return connection.transcieve(command);
            } catch (CardLostException e) {
                cardLost(connection, e);

                throw e;
            }
        }

        /**
         * A card which does not respond to a command is lost, like with Android's own NFC reader, until it is removed
         * and presented again. Tell the client, unless the card was removed (and reported) or polling ended meanwhile.
         */

        private void cardLost(CardConnection connection, CardLostException e) {
            synchronized (lock) {
                if(connection.lost) {
                    return;
                }
                connection.lost = true;

                if(this.connection == connection) {
                    LOGGER.info("Card lost: " + e.getMessage());

                    cardServer.cardLost();
                } else {
                    LOGGER.info("Previous card lost: " + e.getMessage());
                }
            }
        }

        /**
         * @return the card listener for the current polling session, or null if not polling
         */

        CardListener getCardListener() {
            synchronized (lock) {
                return session;
            }
        }

        @Override
        public boolean onConnect(List<String> tags) {
            PooledCardTerminal current = this.pooledCardTerminal;
            if(current != null && !current.isClosed() && current.matches(tags)) {
                // already connected, do not borrow another reader
                LOGGER.info("Already connected to reader " + current.getCardTerminal().getName());

                return true;
            }

            if(current != null) {
                // another reader, or the current one was closed: return the current reader to the pool first
                LOGGER.info("Release reader " + current.getCardTerminal().getName());

                release();
            }

            PooledCardTerminal borrow = cardTerminalsPollingPool.borrow(tags);
            if(borrow != null) {
                LOGGER.info("Connected reader " + borrow.getCardTerminal().getName() );
                this.pooledCardTerminal = borrow;

                return true;
            }
            LOGGER.info("Unable to connect reader");

            return false;
        }

        /**
         * End polling and return the reader to the pool.
         */

        void release() {
            try {
                onEndPolling();
            } catch (CardException e) {
                LOGGER.info("Problem ending polling", e);
            }
            onDisconnect();
        }

        @Override
        public boolean onDisconnect() {
            LOGGER.info("on disconnect reader");

            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null) {
                this.pooledCardTerminal = null;

                if(!pooledCardTerminal.isClosed()) {
                    cardTerminalsPollingPool.unborrow(pooledCardTerminal);
                }
            }

            return true;
        }

        @Override
        public boolean onBeginPolling() throws CardException {
            PooledCardTerminal pooledCardTerminal = this.pooledCardTerminal;
            if(pooledCardTerminal != null && !pooledCardTerminal.isClosed()) {
                LOGGER.info("Begin polling " + pooledCardTerminal.getCardTerminal().getName());
                PollingSession session;
                synchronized (lock) {
                    session = this.session;
                    if(session == null || session.pooledCardTerminal != pooledCardTerminal) {
                        session = new PollingSession(pooledCardTerminal);
                        this.session = session;
                    }
                    // else already polling, keep the session so that the current card is still handled
                }
                pooledCardTerminal.setListener(session);
                pooledCardTerminal.startPolling();
                return true;
            }
            LOGGER.info("Cannot begin polling, no reader");

            return false;
        }

        @Override
        public boolean onEndPolling() throws CardException {
            CardConnection connection;
            synchronized (lock) {
                // card events still in flight from this polling session are ignored from now on
                this.session = null;

                connection = this.connection;
                this.connection = null;
            }
            if(connection != null) {
                LOGGER.info("Disconnect card");
                try {
                    connection.card.beginExclusive();
                    connection.card.disconnect(true);
                    // end exclusive not necessary here
                    //card.endExclusive();
                    LOGGER.info("Disconnected card");
                } catch(Exception e) {
                    LOGGER.info("Disconnected card", e);
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

    public WebSocketNfcServer(int port, CardTerminalsFilter cardTerminalsFilter, ExtendedCardTerminalFactory extendedCardTerminalFactory) {
        super(new InetSocketAddress(port));

        this.cardTerminalsFilter = cardTerminalsFilter;
        this.cardTerminalsPollingPool = new CardTerminalsPollingPool(extendedCardTerminalFactory);
    }

    public WebSocketNfcServer(InetSocketAddress address, CardTerminalsFilter cardTerminalsFilter, ExtendedCardTerminalFactory extendedCardTerminalFactory) {
        super(address);
        this.cardTerminalsFilter = cardTerminalsFilter;
        this.cardTerminalsPollingPool = new CardTerminalsPollingPool(extendedCardTerminalFactory);
    }

    public WebSocketNfcServer(int port, Draft_6455 draft, CardTerminalsFilter cardTerminalsFilter, ExtendedCardTerminalFactory extendedCardTerminalFactory) {
        super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
        this.cardTerminalsFilter = cardTerminalsFilter;
        this.cardTerminalsPollingPool = new CardTerminalsPollingPool(extendedCardTerminalFactory);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info("onOpen");

        conn.setAttachment(createAttachment(new WebSocketNfcMessageWriter(conn)));
    }

    /**
     * Create the state for a client connection.
     *
     * @param writer sends messages to the client
     * @return attachment
     */

    Attachment createAttachment(NfcMessageListener writer) {
        Attachment attachment = new Attachment();
        attachment.cardServer = new CardServer(writer);
        attachment.readerServer = new ReaderServer(writer);

        attachment.compositeNfcMessageListener = new CompositeNfcMessageListener();
        attachment.compositeNfcMessageListener.add(attachment.cardServer);
        attachment.compositeNfcMessageListener.add(attachment.readerServer);

        attachment.cardServer.setListener(attachment);

        attachment.readerServer.setListener(attachment);

        return attachment;
    }

    CardTerminalsPollingPool getCardTerminalsPollingPool() {
        return cardTerminalsPollingPool;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("onClose");

        Attachment attachment = conn.getAttachment();
        if(attachment != null) {
            attachment.release();
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