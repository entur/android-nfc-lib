package no.entur.abt.nfc.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

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

    private MainApplication mainApplication;

    private ToggleButton readerStatusButton;
    private ToggleButton tagStatusButton;
    private ToggleButton serviceStatusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setupNfc();

        this.mainApplication = (MainApplication) getApplication();

        serviceStatusButton = (ToggleButton) findViewById(R.id.serviceStatusButton);
        serviceStatusButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                externalNfcServiceCallbackSupport.setEnabled(isChecked);
            }
        });

        readerStatusButton = (ToggleButton) findViewById(R.id.readerStatusButton);
        readerStatusButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                externalNfcReaderCallbackSupport.setEnabled(isChecked);
            }
        });

        tagStatusButton = (ToggleButton) findViewById(R.id.tagStatusButton);
        tagStatusButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                externalNfcTagCallbackSupport.setEnabled(isChecked);
                externalNfcTagLostCallbackSupport.setEnabled(isChecked);
            }
        });
    }


    private void setupNfc() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        threadPoolExecutor.setRejectedExecutionHandler((r, executor) -> {
            LOGGER.error( "Rejected execution for " + r + " " + executor);
        });

        externalNfcTagCallbackSupport = new ExternalNfcTagCallbackSupport(this, this, threadPoolExecutor);
        externalNfcTagCallbackSupport.setEnabled(true);

        externalNfcServiceCallbackSupport = new ExternalNfcServiceCallbackSupport(this, this, threadPoolExecutor);
        externalNfcServiceCallbackSupport.setEnabled(true);

        externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, threadPoolExecutor);
        externalNfcReaderCallbackSupport.setEnabled(true);

        externalNfcTagLostCallbackSupport = new ExternalNfcTagLostCallbackSupport(this, this, threadPoolExecutor);
        externalNfcTagLostCallbackSupport.setEnabled(true);
    }

    @Override
    public void onExternalNfcReaderOpened(Intent intent) {
        if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
            AcrReader reader = (AcrReader) intent.getParcelableExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL);

            LOGGER.info("Got reader type " + reader.getClass().getName() + " in activity");
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
        LOGGER.info("External Tag discovered in activity");
        runOnUiThread(() -> {
            setTagPresent(true);
        });
    }

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("Tag discovered in activity");
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
        externalNfcReaderCallbackSupport.onResume();
        externalNfcTagLostCallbackSupport.onResume();
        externalNfcServiceCallbackSupport.onResume();
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
        if (started) {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStarted);
        } else {
            setTextViewText(R.id.serviceStatus, R.string.serviceStatusStopped);
        }

        setTextViewText(R.id.readerStatus, R.string.tagStatusNone);
        setTextViewText(R.id.tagStatus, R.string.tagStatusNone);
    }

    public void setReaderOpen(final boolean open) {
        if (open) {
            setTextViewText(R.id.readerStatus, R.string.readerStatusOpen);
        } else {
            setTextViewText(R.id.readerStatus, R.string.readerStatusClosed);

            setTextViewText(R.id.tagStatus, R.string.tagStatusNone);
        }
    }

    public void setTagPresent(final boolean present) {
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

    public void stopReaderService(View view) {
        LOGGER.info("Stop reader service");

        mainApplication.setExternalNfcReader(false);
    }

    public void startReaderService(View view) {
        LOGGER.info("Start reader service");

        mainApplication.setExternalNfcReader(true);
    }

}