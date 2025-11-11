package no.entur.abt.nfc.example;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.test.MockTag;
import no.entur.android.nfc.external.test.tech.transceive.ListMockTransceive;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.websocket.android.WebSocketNfcService;


@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MockTagTest {

	protected static final byte[] SELECT_PPSE_COMMAND = new byte[] {0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};

	protected static final  byte[] SELECT_TRANSPORT_APPLICATION_COMMAND = ByteArrayHexStringConverter.hexStringToByteArray("D2760000850101");

	private static final Logger LOGGER = LoggerFactory.getLogger(MockTagTest.class);

	public ActivityScenarioRule rule = new ActivityScenarioRule(MainActivity.class);

	@Rule
	public RuleChain chain;

	public MockTagTest() {
		chain = RuleChain.outerRule(rule);
	}

	@Test
	public void connectReader() throws Exception {
		LOGGER.debug("Connect reader");

		rule.getScenario().onActivity(activity -> {

			MockTag mockTag = MockTag.newBuilder()
					.withContext(activity)
					.withTagId(new byte[]{0x04, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06})
					.withIsoDep( (isoDep) -> {
						isoDep.withHiLayer(new byte[]{0x03, 0x04});
						isoDep.withHistoricalBytes(new byte[]{(byte) 0x80}); // desfire
						isoDep.withTransceive(ListMockTransceive.newBuilder()
								.withErrorResponse(new byte[]{0x63})
								.withTransceive(ByteArrayHexStringConverter.hexStringToByteArray("5A008057"), new byte[]{(byte) 0x00})
								.build());
						}
					)
					.build();

			mockTag.power();
        });

		Thread.sleep(20000);
	}
}
