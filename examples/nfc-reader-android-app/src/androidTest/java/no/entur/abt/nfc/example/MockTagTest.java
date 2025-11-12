package no.entur.abt.nfc.example;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.test.MockExternalReader;
import no.entur.android.nfc.wrapper.test.MockTag;
import no.entur.android.nfc.wrapper.test.tech.transceive.ListMockTransceive;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import java.util.concurrent.ThreadPoolExecutor;

@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MockTagTest {

	public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

	@Rule
	public RuleChain chain;

	private MainApplication mainApplication;

	public MockTagTest() {
		chain = RuleChain.outerRule(rule);
	}


	@Test
	public void testDesfireEV1DirectMethodInvocation() throws Exception {
		rule.getScenario().onActivity(activity -> {
			mainApplication = (MainApplication) activity.getApplication();

			MockTag mockTag = MockTag.newBuilder()
					.withRandomTagId()
					.withIsoDep( (isoDep) -> {
								isoDep.withDesfireEV1(); // desfire
								isoDep.withTransceive(ListMockTransceive.newBuilder()
										.withErrorResponse("63") // raw desfire response
										.withTransceiveNativeDesfireSelectApplication("008057", "00") // raw desfire command
										.build());
							}
					)
					.build();

			activity.onExternalTagDiscovered(mockTag, null);
		});

		waitForThreadExecutor();
		onView(withId(R.id.tagStatus)).check(matches(withText("Present")));
	}

	private void waitForThreadExecutor() throws InterruptedException {
		ThreadPoolExecutor threadPoolExecutor = mainApplication.getThreadPoolExecutor();

		while(threadPoolExecutor.getActiveCount() > 0 || threadPoolExecutor.getQueue().size() > 0) {
			Thread.sleep(100);
		}
	}

	@Test
	public void testDesfireEV1ViaIntent() throws Exception {
		rule.getScenario().onActivity(activity -> {
			mainApplication = (MainApplication) activity.getApplication();

			MockTag mockTag = MockTag.newBuilder()
					.withRandomTagId()
					.withIsoDep( (isoDep) -> {
						isoDep.withDesfireEV1(); // desfire
						isoDep.withTransceive(ListMockTransceive.newBuilder()
								.withErrorResponse("63") // raw desfire response
								.withTransceiveNativeDesfireSelectApplication("008057", "00") // raw desfire command
								.build());
						}
					)
					.build();

			MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

			mockExternalReader.open();

			mockExternalReader.tagEnteredField(mockTag);
		});

		waitForThreadExecutor();
		onView(withId(R.id.tagStatus)).check(matches(withText("Present")));
    }

    @Test
    public void testUltralightViaIntent() throws Exception {
        rule.getScenario().onActivity(activity -> {
			mainApplication = (MainApplication) activity.getApplication();

            MockTag mockTag = MockTag.newBuilder()
                    .withMifareUltralight( (ul) -> {
                        ul.withMemoryLayout((mem) -> {
                           mem.withPage(3, new byte[]{0x00, 0x01, 0x02, 0x03});
                        });
                    })
                    .build();

			MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

			mockExternalReader.open();

			mockExternalReader.tagEnteredField(mockTag);
		});
		waitForThreadExecutor();
		onView(withId(R.id.tagStatus)).check(matches(withText("Present")));
    }
}
