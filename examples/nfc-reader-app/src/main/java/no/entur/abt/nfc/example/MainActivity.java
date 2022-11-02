package no.entur.abt.nfc.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagLostCallback;
import no.entur.android.nfc.external.ExternalNfcTagLostCallbackSupport;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.wrapper.Tag;

public class MainActivity extends AppCompatActivity implements ExternalNfcTagCallback, ExternalNfcReaderCallback, ExternalNfcServiceCallback, ExternalNfcTagLostCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private ExternalNfcTagCallbackSupport externalNfcTagCallbackSupport;
    private ExternalNfcServiceCallbackSupport externalNfcServiceCallbackSupport;
    private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;
    private ExternalNfcTagLostCallbackSupport externalNfcTagLostCallbackSupport;

    private ThreadPoolExecutor threadPoolExecutor;
    private boolean usbRunning = false;

    private MainApplication mainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupNfc();

        this.mainApplication = (MainApplication) getApplication();
    }


    private void setupNfc() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        threadPoolExecutor.setRejectedExecutionHandler((r, executor) -> {
            LOGGER.error( "Rejected execution for " + r + " " + executor);
        });

        externalNfcTagCallbackSupport = new ExternalNfcTagCallbackSupport(this, this, threadPoolExecutor);
        externalNfcTagCallbackSupport.setEnabled(true);

        externalNfcServiceCallbackSupport = new ExternalNfcServiceCallbackSupport(this, this, threadPoolExecutor);

        externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, threadPoolExecutor);
        externalNfcReaderCallbackSupport.setEnabled(true);

        externalNfcTagLostCallbackSupport = new ExternalNfcTagLostCallbackSupport(this, this, threadPoolExecutor);
        externalNfcTagLostCallbackSupport.setEnabled(true);
    }

    @Override
    public void onExternalNfcReaderOpened(Intent intent) {
        if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
            AcrReader reader = (AcrReader) intent.getParcelableExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL);

            LOGGER.info("Got reader type " + reader.getClass().getName());
        }

        runOnUiThread(() -> {
            setReaderOpen(true);
        });
    }


    @Override
    public void onExternalNfcReaderClosed(Intent intent) {
        // do nothing
        runOnUiThread(() -> {
            setReaderOpen(false);
        });
    }

    @Override
    public void onExternalTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("External Tag discovered");
        runOnUiThread(() -> {
            setTagPresent(true);
        });
    }

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("Tag discovered");
        runOnUiThread(() -> {
            setTagPresent(true);
        });
    }

    @Override
    public void onExternalTagLost(Intent intent) {
        runOnUiThread(() -> {
            setTagPresent(false);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        externalNfcTagCallbackSupport.onPause();
        externalNfcServiceCallbackSupport.onPause();
        externalNfcReaderCallbackSupport.onPause();
        externalNfcTagLostCallbackSupport.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        externalNfcTagCallbackSupport.onResume();
        externalNfcServiceCallbackSupport.onResume();
        externalNfcReaderCallbackSupport.onResume();
        externalNfcTagLostCallbackSupport.onResume();
    }

    public void onExternalNfcServiceStopped(Intent intent) {
        LOGGER.info("External NFC service stopped");
        runOnUiThread(() -> {
            setUsbServiceStarted(false);
        });
    }

    public void onExternalNfcServiceStarted(Intent intent) {
        LOGGER.info("External NFC service started");
        runOnUiThread(() -> {
            setUsbServiceStarted(true);
        });
    }

    public void setUsbServiceStarted(final boolean started) {
        this.usbRunning = started;

        if (started) {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);

            setViewVisibility(R.id.readerStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);

            setViewVisibility(R.id.readerStatusRow, View.GONE);
            setViewVisibility(R.id.tagStatusRow, View.GONE);
        }

        Button start = (Button) findViewById(R.id.startService);
        if (started) {
            start.setText(R.string.stopService);
        } else {
            start.setText(R.string.startService);
        }
    }

    public void setReaderOpen(final boolean open) {
        if (open) {
            setTextViewText(R.id.readerStatus, R.string.readerStatusOpen);

            setViewVisibility(R.id.tagStatusRow, View.VISIBLE);
        } else {
            setTextViewText(R.id.readerStatus, R.string.readerStatusClosed);

            setViewVisibility(R.id.tagStatusRow, View.GONE);
        }
    }

    private void setViewVisibility(int id, int visibility) {
        View view = findViewById(id);
        view.setVisibility(visibility);
    }

    public void setTagPresent(final boolean present) {
        setViewVisibility(R.id.tagStatusRow, View.VISIBLE);

        if (present) {
            setTextViewText(R.id.tagStatus, R.string.tagStatusPresent);
        } else {
            setTextViewText(R.id.tagStatus, R.string.tagStatusAbsent);
        }
    }

    public void setTextViewText(final int resource, final int string) {
        setTextViewText(resource, getString(string));
    }

    public void setTextViewText(final int resource, final String string) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView textView = (TextView) findViewById(resource);
                textView.setText(string);
                textView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void startReaderService(View view) {

        if (usbRunning) {
            LOGGER.info("Stop reader service");

            mainApplication.setExternalNfcReader(false);
        } else {
            LOGGER.info("Start reader service");

            mainApplication.setExternalNfcReader(true);
        }
    }

}