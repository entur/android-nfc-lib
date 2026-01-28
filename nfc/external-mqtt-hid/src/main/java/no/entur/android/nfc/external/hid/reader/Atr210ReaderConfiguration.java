package no.entur.android.nfc.external.hid.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Atr210ReaderConfiguration implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderService.class);

	private final ExecutorService executorService;
    private final long timeout;

    private boolean enableHfReader;
    private boolean enableSamReader;

    private boolean autoConfiguration;

    public class Atr210OpenRunnable implements Runnable {

        private final Atr210ReaderService service;

        public Atr210OpenRunnable(Atr210ReaderService service) {
            this.service = service;
        }

        @Override
        public void run() {
            try {
                service.subscribe();

                if(autoConfiguration) {
                    // do this before read broadcast is passed up to app
                    service.configureNfcReaders(enableHfReader, enableSamReader, timeout);
                }
            } catch(Exception e) {
                LOGGER.warn("Problem opening reader", e);
            } finally {
                service.broadcastOpened();
            }
        }
    }

    public Atr210ReaderConfiguration(long timeout) {
		this(timeout, Executors.newSingleThreadExecutor());
	}

	public Atr210ReaderConfiguration(long timeout, ExecutorService executorService) {
		this.timeout = timeout;
		this.executorService = executorService;
    }

	public void open(Atr210ReaderService service) {
        executorService.submit(new Atr210OpenRunnable(service));
	}

    public void close() {
        executorService.shutdownNow();
    }

    public void setAutoConfiguration(boolean autoConfiguration) {
        this.autoConfiguration = autoConfiguration;
    }

    public void setNfcReaders(boolean hf, boolean sam) {
        this.enableHfReader = hf;
        this.enableSamReader = sam;
    }

}
