package no.entur.abt.nfc.example;

import static androidx.test.espresso.Espresso.pressBack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiSelector;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mService = (WebSocketNfcService.LocalBinder)service;
			bound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected&mdash;that is, its process crashed.
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
			applicationContext.bindService(new Intent(applicationContext, WebSocketNfcService.class), mConnection,
					Context.BIND_AUTO_CREATE);
			Thread.sleep(1000);

			if(bound) {
				LOGGER.info("Service bound");
				try {
					if(mService.connect("ws://10.0.2.2:3000")) {
						mService.connectReader();

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
			if(mConnection != null) {
				applicationContext.unbindService(mConnection);
			}
		}




	}
}
