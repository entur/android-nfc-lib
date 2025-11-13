package no.entur.abt.nfc.example;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

import no.entur.android.nfc.external.test.MockExternalReader;


@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public class MockExternalReaderTest {

	public ActivityScenarioRule rule = new ActivityScenarioRule(MainActivity.class);

	@Rule
	public RuleChain chain;

	public MockExternalReaderTest() {
		chain = RuleChain.outerRule(rule);
	}

	private MainApplication mainApplication;

    @Test
    public void testReader() throws Exception {
        rule.getScenario().onActivity(activity -> {
			mainApplication = (MainApplication) activity.getApplication();

			MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

			mockExternalReader.open();
        });

		waitForThreadExecutor();
    }

	private void waitForThreadExecutor() throws InterruptedException {
		ThreadPoolExecutor threadPoolExecutor = mainApplication.getThreadPoolExecutor();

		while(threadPoolExecutor.getActiveCount() > 0 || threadPoolExecutor.getQueue().size() > 0) {
			Thread.sleep(100);
		}
	}

}
