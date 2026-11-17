package no.entur.android.nfc.websocket.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.tech.TagTechnology;
import no.entur.android.nfc.websocket.client.WebSocketClient;
import no.entur.android.nfc.websocket.client.WebSocketClientFactory;
import no.entur.android.nfc.websocket.client.WebSocketClientListener;
import no.entur.android.nfc.websocket.messages.RequestResponseMessages;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;
import okhttp3.WebSocket;

public class WebSocketNfcServiceTest {

    private static final List<String> ISO_DEP = Collections.singletonList("IsoDep");
    private static final byte[] UID = new byte[]{0x04, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66};

    private static final List<String> MIFARE_ULTRALIGHT = Collections.singletonList("MifareUltralight");
    private static final byte[] ULTRALIGHT_ATR = new byte[]{0x3B, (byte) 0x8F, (byte) 0x80, 0x01, (byte) 0x80, 0x4F, 0x0C, (byte) 0xA0, 0x00, 0x00, 0x03, 0x06, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x68};
    private static final byte[] ULTRALIGHT_HISTORICAL_BYTES = new byte[]{(byte) 0x80, 0x4F, 0x0C, (byte) 0xA0, 0x00, 0x00, 0x03, 0x06, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00};

    private WebSocketNfcService service;
    private WebSocketClientFactory factory;

    @Before
    public void before() {
        service = spy(new WebSocketNfcService());
        doNothing().when(service).sendBroadcast(any(Intent.class), anyString());

        factory = mock(WebSocketClientFactory.class);
        service.factory = factory;
    }

    @Test
    public void testCardEventsFromPreviousConnectionAreIgnored() {
        WebSocketClient first = newClient();
        WebSocketClient second = newClient();
        when(factory.connect(eq("ws://first"), any())).thenReturn(first);
        when(factory.connect(eq("ws://second"), any())).thenReturn(second);

        assertTrue(service.connect("ws://first"));
        assertTrue(service.beginPolling());
        CardClient.Listener firstListener = captureCardListener(first);

        assertTrue(service.connect("ws://second"));
        assertTrue(service.beginPolling());
        CardClient.Listener secondListener = captureCardListener(second);

        // previous connection no longer delivers card events
        verify(first.getCardClient()).setListener(null);

        secondListener.onCardPresent(second.getCardClient(), ISO_DEP, null, null, UID);
        TagProxy tag = currentTag();

        // late events from the previous connection, i.e. already in flight
        firstListener.onCardLost();
        firstListener.onCardPresent(first.getCardClient(), ISO_DEP, null, null, UID);

        assertTrue(tag.isPresent());
        assertEquals(1, service.store.getItems().size());
        verify(service, never()).sendTagLeftField(any());

        secondListener.onCardLost();

        assertFalse(tag.isPresent());
        verify(service).sendTagLeftField(tag);
    }

    @Test
    public void testConnectionFailureLosesTag() {
        WebSocketClient client = newClient();
        ArgumentCaptor<WebSocketClientListener> clientListener = ArgumentCaptor.forClass(WebSocketClientListener.class);
        when(factory.connect(eq("ws://reader"), clientListener.capture())).thenReturn(client);

        assertTrue(service.connect("ws://reader"));
        assertTrue(service.beginPolling());
        CardClient.Listener cardListener = captureCardListener(client);

        cardListener.onCardPresent(client.getCardClient(), ISO_DEP, null, null, UID);
        TagProxy tag = currentTag();

        // socket fails while the card is present, so no card lost will arrive
        clientListener.getValue().onClosed();

        assertFalse(tag.isPresent());
        verify(service).sendTagLeftField(tag);
        verify(client.getCardClient()).setListener(null);

        // a late card lost from the closed connection does not send another tag left field
        cardListener.onCardLost();
        verify(service, times(1)).sendTagLeftField(any());
    }

    @Test
    public void testMifareUltralightWithoutUid() {
        CardClient.Listener cardListener = connectAndBeginPolling();

        // the server sends an empty UID if it could not be read
        cardListener.onCardPresent(client.getCardClient(), MIFARE_ULTRALIGHT, ULTRALIGHT_ATR, ULTRALIGHT_HISTORICAL_BYTES, new byte[0]);

        currentTag();
    }

    @Test
    public void testTagWhichCannotBeCreatedIsNotKept() {
        CardClient.Listener cardListener = connectAndBeginPolling();

        // i.e. not an NXP UID
        cardListener.onCardPresent(client.getCardClient(), MIFARE_ULTRALIGHT, ULTRALIGHT_ATR, ULTRALIGHT_HISTORICAL_BYTES, new byte[]{0x05, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66});

        assertTrue(service.store.getItems().isEmpty());

        // the next card works
        cardListener.onCardPresent(client.getCardClient(), MIFARE_ULTRALIGHT, ULTRALIGHT_ATR, ULTRALIGHT_HISTORICAL_BYTES, UID);
        currentTag();
    }

    @Test
    public void testCardPresentWithoutCardLostSendsTagLeftField() {
        CardClient.Listener cardListener = connectAndBeginPolling();

        cardListener.onCardPresent(client.getCardClient(), ISO_DEP, null, null, UID);
        TagProxy first = currentTag();

        // card lost was not received
        cardListener.onCardPresent(client.getCardClient(), ISO_DEP, null, null, UID);

        assertFalse(first.isPresent());
        verify(service).sendTagLeftField(first);
    }

    @Test
    public void testTimeoutIsAppliedToCardClient() throws Exception {
        WebsocketReaderTechnology readerTechnology = service.readerTechnology;

        connectAndBeginPolling();
        verify(client.getCardClient()).setTimeout(WebsocketReaderTechnology.DEFAULT_TIMEOUT);

        assertEquals(ErrorCodes.SUCCESS, readerTechnology.setTimeout(TagTechnology.ISO_DEP, 5000));
        assertEquals(5000, readerTechnology.getTimeout(TagTechnology.ISO_DEP));
        verify(client.getCardClient()).setTimeout(5000);

        // the remote reader adds a network round trip, so not less than the default
        readerTechnology.setTimeout(TagTechnology.ISO_DEP, 100);
        verify(client.getCardClient(), times(2)).setTimeout(WebsocketReaderTechnology.DEFAULT_TIMEOUT);
        readerTechnology.setTimeout(TagTechnology.ISO_DEP, 5000);

        // the next connection gets the same timeout
        WebSocketClient next = newClient();
        when(factory.connect(eq("ws://next"), any())).thenReturn(next);
        assertTrue(service.connect("ws://next"));
        verify(next.getCardClient()).setTimeout(5000);

        readerTechnology.resetTimeouts();
        verify(next.getCardClient()).setTimeout(WebsocketReaderTechnology.DEFAULT_TIMEOUT);
    }

    private WebSocketClient client;

    private CardClient.Listener connectAndBeginPolling() {
        client = newClient();
        when(factory.connect(eq("ws://reader"), any())).thenReturn(client);

        assertTrue(service.connect("ws://reader"));
        assertTrue(service.beginPolling());
        return captureCardListener(client);
    }

    private TagProxy currentTag() {
        assertEquals(1, service.store.getItems().size());
        TagProxy tag = service.store.getItems().get(0);
        assertTrue(tag.isPresent());
        return tag;
    }

    private static CardClient.Listener captureCardListener(WebSocketClient client) {
        ArgumentCaptor<CardClient.Listener> captor = ArgumentCaptor.forClass(CardClient.Listener.class);
        verify(client.getCardClient()).setListener(captor.capture());
        return captor.getValue();
    }

    private static WebSocketClient newClient() {
        ReaderClient readerClient = mock(ReaderClient.class);
        when(readerClient.beginPolling()).thenReturn(true);

        WebSocketClient client = new WebSocketClient();
        client.setCardClient(mock(CardClient.class));
        client.setReaderClient(readerClient);
        client.setWebSocket(mock(WebSocket.class));
        client.setClosable(mock(RequestResponseMessages.class));
        return client;
    }
}
