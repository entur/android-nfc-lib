package no.entur.abt.nfc.example;

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

import no.entur.android.nfc.websocket.android.WebSocketNfcService;


@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class WebsocketReaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketReaderTest.class);

	public ActivityScenarioRule rule = new ActivityScenarioRule(MainActivity.class);

	@Rule
	public RuleChain chain;

	/** Flag indicating whether we have called bind on the service. */
	boolean bound;

	private WebSocketNfcService.LocalBinder mService;
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = (WebSocketNfcService.LocalBinder)service;
			bound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			bound = false;
		}
	};

	public WebsocketReaderTest() {
		chain = RuleChain.outerRule(rule);
	}

	@Test
	public void connectReader() throws Exception {
		LOGGER.debug("Connect reader");

		Context applicationContext = ApplicationProvider.getApplicationContext();

		applicationContext.startService(new Intent(applicationContext, WebSocketNfcService.class));

		try {
			applicationContext.bindService(new Intent(applicationContext, WebSocketNfcService.class), connection,
					Context.BIND_AUTO_CREATE);
			Thread.sleep(1000);

			if(bound) {
				LOGGER.info("Service bound");
				try {
					if(mService.connect("ws://127.0.0.1:3001")) {
						mService.connectReader(new String[]{});

						mService.beginPolling();

						System.out.println("Begin waiting for card..");
						Thread.sleep(60000);

						System.out.println("End waiting for card");
						mService.endPolling();

						mService.disconnectReader();

						mService.disconnect();
					}


				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				LOGGER.info("Service not bound");
			}

		} finally {
			if(connection != null) {
				applicationContext.unbindService(connection);
			}
		}
	}
}
