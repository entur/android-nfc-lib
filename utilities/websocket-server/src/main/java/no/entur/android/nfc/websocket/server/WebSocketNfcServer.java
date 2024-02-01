package no.entur.android.nfc.websocket.server;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private AtomicInteger counter = new AtomicInteger();

  private static class Attachment {

      private CardServer cardServer;

      private ReaderServer readerServer;

      private CompositeNfcMessageListener compositeNfcMessageListener;
  }

  public WebSocketNfcServer(int port) throws UnknownHostException {
    super(new InetSocketAddress(port));
  }

  public WebSocketNfcServer(InetSocketAddress address) {
    super(address);
  }

  public WebSocketNfcServer(int port, Draft_6455 draft) {
    super(new InetSocketAddress(port), Collections.<Draft>singletonList(draft));
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

    CardServer.Listener l = new CardServer.Listener() {
      @Override
      public byte[] transcieve(byte[] message) {
        return new byte[]{0x01, 0x04};
      }
    };
    attachment.cardServer.setListener(l);

    ReaderServer.Listener ll = new ReaderServer.Listener() {

      @Override
      public boolean onConnect() {
        LOGGER.info("on connect reader");
        return true;
      }

      @Override
      public boolean onDisconnect() {
        LOGGER.info("on disconnect reader");
        return true;
      }

      @Override
      public boolean onBeginPolling() {
        LOGGER.info("on begin polling");
        return true;
      }

      @Override
      public boolean onEndPolling() {
        LOGGER.info("on end polling");
        return true;
      }
    };

    attachment.readerServer.setListener(ll);

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
    if(m != null) {
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
  }

}