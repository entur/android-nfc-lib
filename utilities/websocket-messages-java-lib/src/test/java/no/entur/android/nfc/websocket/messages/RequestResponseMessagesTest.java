package no.entur.android.nfc.websocket.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import no.entur.android.nfc.websocket.messages.card.CardApduRequestMessage;
import no.entur.android.nfc.websocket.messages.card.CardApduResponseMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardLostMessage;
import no.entur.android.nfc.websocket.messages.card.broadcast.CardPresentMessage;

public class RequestResponseMessagesTest {

	@Test
	public void testBroadcastWithSameIdAsRequestIsNotTakenAsResponse() {
		List<NfcMessage> local = Collections.synchronizedList(new ArrayList<>());

		AtomicReference<RequestResponseMessages> messages = new AtomicReference<>();
		CardLostMessage[] broadcast = new CardLostMessage[1];
		CardApduResponseMessage[] response = new CardApduResponseMessage[1];

		NfcMessageListener remote = (request) -> {
			// server broadcast happens to have the same id as the request
			broadcast[0] = new CardLostMessage(request.getId());
			messages.get().onMessage(broadcast[0]);

			response[0] = new CardApduResponseMessage(new byte[]{(byte) 0x90, 0x00}, request.getId());
			messages.get().onMessage(response[0]);
		};

		messages.set(new RequestResponseMessages(local::add, remote));

		NfcMessage result = messages.get().sendAndWaitForResponse(new CardApduRequestMessage(new byte[]{0x00}), 1000);

		assertSame(response[0], result);
		assertEquals(1, local.size());
		assertSame(broadcast[0], local.get(0));
	}

	@Test
	public void testDelayedResponseIsForwardedToLocalListener() {
		List<NfcMessage> local = Collections.synchronizedList(new ArrayList<>());

		RequestResponseMessages messages = new RequestResponseMessages(local::add, (m) -> {});

		CardApduResponseMessage response = new CardApduResponseMessage(new byte[]{(byte) 0x90, 0x00}, 12345);
		messages.onMessage(response);

		assertEquals(1, local.size());
	}

	@Test
	public void testCloseWakesWaitingRequest() throws Exception {
		RequestResponseMessages messages = new RequestResponseMessages((m) -> {}, (m) -> {});

		CountDownLatch done = new CountDownLatch(1);
		AtomicReference<NfcMessage> result = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			result.set(messages.sendAndWaitForResponse(new CardApduRequestMessage(new byte[]{0x00}), 60000));
			done.countDown();
		});
		thread.start();

		Thread.sleep(100);
		messages.close();

		assertTrue(done.await(5, TimeUnit.SECONDS));
		assertNull(result.get());
	}

	@Test
	public void testRequestAfterCloseDoesNotWait() {
		List<NfcMessage> remote = Collections.synchronizedList(new ArrayList<>());
		RequestResponseMessages messages = new RequestResponseMessages((m) -> {}, remote::add);

		messages.close();

		long time = System.currentTimeMillis();
		assertNull(messages.sendAndWaitForResponse(new CardApduRequestMessage(new byte[]{0x00}), 60000));
		assertTrue(System.currentTimeMillis() - time < 5000);
		assertTrue(remote.isEmpty());
	}

	@Test
	public void testLockIgnoresSpuriousWakeup() throws Exception {
		RequestResponseMessageLock lock = new RequestResponseMessageLock(new CardApduRequestMessage(new byte[]{0x00}));

		CountDownLatch done = new CountDownLatch(1);
		AtomicReference<NfcMessage> result = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			result.set(lock.waitForMessage(60000));
			done.countDown();
		});
		thread.start();

		Thread.sleep(100);
		synchronized (lock) {
			// i.e. a wakeup without a response
			lock.notifyAll();
		}
		assertTrue(!done.await(200, TimeUnit.MILLISECONDS));

		CardApduResponseMessage response = new CardApduResponseMessage(new byte[]{(byte) 0x90, 0x00}, 1);
		lock.onMessage(response);

		assertTrue(done.await(5, TimeUnit.SECONDS));
		assertSame(response, result.get());
	}

	@Test
	public void testLockTimeout() {
		RequestResponseMessageLock lock = new RequestResponseMessageLock(new CardApduRequestMessage(new byte[]{0x00}));

		long time = System.currentTimeMillis();
		assertNull(lock.waitForMessage(100));
		assertTrue(System.currentTimeMillis() - time >= 90);
	}

	@Test
	public void testCardPresentWithoutUid() {
		CardPresentMessage message = new CardPresentMessage(Collections.singletonList("IsoDep"), new byte[]{0x3B, 0x00}, new byte[0], null);

		byte[] bytes = new NfcMessageWriter().write(message);

		CardPresentMessage parsed = (CardPresentMessage) new NfcMessageReader().parse(bytes);

		assertEquals(0, parsed.getUid().length);
		assertEquals(2, parsed.getAtr().length);
		assertEquals(0, parsed.getHistoricalBytes().length);
		assertEquals(Collections.singletonList("IsoDep"), parsed.getTechnologies());
	}

}
