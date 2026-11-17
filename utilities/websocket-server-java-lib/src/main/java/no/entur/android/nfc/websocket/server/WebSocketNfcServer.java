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
     * A card connection, possibly reconnected. Replaced as a whole, so that the card and channel always match.
     */

    private static final class CardConnection {

        private final Card card;
        private final CardChannel channel;

        private CardConnection(Card card) {
            this.card = card;
            this.channel = card.getBasicChannel();
        }

        private byte[] transmit(byte[] command) throws IOException {
            try {
                // as-is, so that i.e. native DESFire commands and responses are not mangled
                return RawCardChannel.transmit(card, channel, command);
            } catch (CardException e) {
                throw new IOException(e);
            }
        }
    }

    class Attachment implements CardListener, CardServer.Listener, ReaderServer.Listener {

        private CardServer cardServer;

        private ReaderServer readerServer;

        private CompositeNfcMessageListener compositeNfcMessageListener;

        private volatile PooledCardTerminal pooledCardTerminal;

        // set by the polling thread, used by the web socket thread
        private volatile CardConnection connection;

        // native MIFARE Ultralight commands must be translated to PC/SC commands
        private volatile boolean mifareUltralight;

        // DESFire on a reader which appends a status word to single byte (native) responses
        private volatile boolean stripAppendedStatusWord;
        private final MifareUltralightPcscAdapter mifareUltralightPcscAdapter = new MifareUltralightPcscAdapter();

        @Override
        public void cardConnected(Card card) throws CardException {
            LOGGER.info("Card connected");
            try {
                connected(card);
            } catch (CardException | RuntimeException e) {
                // the polling server does not report a card as disconnected if connecting it failed,
                // so release a reconnected card here
                reset(card);

                throw e;
            }
        }

        private void connected(Card card) throws CardException {
            this.connection = new CardConnection(card);

            ATR atr = card.getATR();

            ExtendedCardTerminal cardTerminal = pooledCardTerminal.getCardTerminal();

            List<String> technologies = new ArrayList<>(cardTerminal.identifyTechnologies(card, connection.channel));

            mifareUltralightPcscAdapter.reset();
            if(MifareUltralightPcscAdapter.isMifareUltralightCandidate(atr.getHistoricalBytes())) {
                readVersionAndSignature(cardTerminal);

                if(mifareUltralightPcscAdapter.hasMifareUltralightVersion() && !technologies.contains("MifareUltralight")) {
                    // i.e. NTAG
                    technologies.add("MifareUltralight");
                }
            }

            LOGGER.info("Got technologies " + technologies);

            this.mifareUltralight = technologies.contains("MifareUltralight");
            this.stripAppendedStatusWord = DesfireResponseAdapter.isDesfire(atr.getBytes()) && cardTerminal.isStatusWordAppendedToSingleByteResponses();

            // empty if the UID could not be read
            byte[] uid = new byte[0];

            // note: might have been reconnected
            CardConnection current = this.connection;
            current.card.beginExclusive();
            try {
                ResponseAPDU response = current.channel.transmit(new CommandAPDU(GET_TAG_ID));
                if (isSuccess(response)) {
                    uid = response.getData();

                    LOGGER.info("Read UID " + ByteArrayHexStringConverter.toHexString(uid));
                } else {
                    LOGGER.info("Unable to read UID");
                }
            } catch (CardException e) {
                LOGGER.info("Unable to read UID", e);
            } finally {
                current.card.endExclusive();
            }

            cardServer.cardPresent(technologies, atr.getBytes(), atr.getHistoricalBytes(), uid);
        }

        /**
         * Read GET_VERSION and READ_SIG via the reader, if supported. This re-activates the card,
         * so reconnect afterwards.
         */

        private void readVersionAndSignature(ExtendedCardTerminal cardTerminal) throws CardException {
            boolean reactivated;
            try {
                reactivated = mifareUltralightPcscAdapter.readVersionAndSignature(cardTerminal);
            } catch (CardException e) {
                LOGGER.warn("Unable to read version and signature", e);

                // might have been re-activated
                reactivated = true;
            }
            if(reactivated) {
                LOGGER.info("Read version " + toHexString(mifareUltralightPcscAdapter.getVersion()) + " and signature " + toHexString(mifareUltralightPcscAdapter.getSignature()) + ", reconnect");

                if(!cardTerminal.waitForCardPresent(REACTIVATION_TIMEOUT)) {
                    throw new CardException("Card not present after re-activation");
                }
                this.connection = new CardConnection(cardTerminal.connect("*"));
            }
        }

        @Override
        public void cardDisconnected(Card card) {
            LOGGER.info("Card disconnected");

            reset(card);

            cardServer.cardLost();
        }

        /**
         * Forget the card, disconnecting it if it was reconnected; the polling server disconnects the original card.
         *
         * @param card the original card
         */

        private void reset(Card card) {
            CardConnection current = this.connection;
            this.connection = null;
            this.mifareUltralight = false;
            this.stripAppendedStatusWord = false;

            if(current != null && current.card != card) {
                try {
                    current.card.disconnect(false);
                } catch (Exception e) {
                    LOGGER.debug("Problem disconnecting reconnected card", e);
                }
            }
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
            connection.card.beginExclusive();
            try {
                if(mifareUltralight) {
                    // one native command might need several APDUs, so translate within the exclusive session
                    return mifareUltralightPcscAdapter.transceive(command, connection::transmit);
                }
                byte[] response = connection.transmit(command);
                if(stripAppendedStatusWord) {
                    response = DesfireResponseAdapter.stripAppendedStatusWord(command, response);
                }
                return response;
            } finally {
                try {
                    connection.card.endExclusive();
                } catch (Exception e) {
                    // i.e. the card was removed; do not hide the response or the original problem
                    LOGGER.debug("Problem ending exclusive access", e);
                }
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
                pooledCardTerminal.setListener(this);
                pooledCardTerminal.startPolling();
                return true;
            }
            LOGGER.info("Cannot begin polling, no reader");

            return false;
        }

        @Override
        public boolean onEndPolling() throws CardException {

            CardConnection connection = this.connection;
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
                } finally {
                    this.connection = null;
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