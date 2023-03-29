package no.entur.abt.nfc.example;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import no.entur.android.nfc.NfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagLostCallback;
import no.entur.android.nfc.external.ExternalNfcTagLostCallbackSupport;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.Tag;

public class MainActivity extends AppCompatActivity implements ExternalNfcTagCallback, ExternalNfcReaderCallback, ExternalNfcServiceCallback, ExternalNfcTagLostCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private ExternalNfcTagCallbackSupport externalNfcTagCallbackSupport;
    private ExternalNfcServiceCallbackSupport externalNfcServiceCallbackSupport;
    private ExternalNfcReaderCallbackSupport externalNfcReaderCallbackSupport;
    private ExternalNfcTagLostCallbackSupport externalNfcTagLostCallbackSupport;

    private NfcReaderCallbackSupport nfcReaderCallbackSupport;

    private ThreadPoolExecutor threadPoolExecutor;

    private MainApplication mainApplication;

    private ToggleButton readerStatusButton;
    private ToggleButton tagStatusButton;
    private ToggleButton serviceStatusButton;

    private View intentDetailsTitle;
    private View intentDetailsTable;
    private TextView intentDetailUuid;
    private TextView intentDetailAction;

    private View tagDetailsTitle;
    private View tagDetailsTable;

    private TextView tagDetailTechTypes;

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

        intentDetailsTitle = findViewById(R.id.intentDetailsTitle);
        intentDetailsTable = findViewById(R.id.intentDetailsTable);
        intentDetailUuid = findViewById(R.id.intentDetailUuid);
        intentDetailAction = findViewById(R.id.intentDetailAction);

        tagDetailsTitle = findViewById(R.id.tagDetailsTitle);
        tagDetailsTable = findViewById(R.id.tagDetailsTable);
        tagDetailTechTypes =  findViewById(R.id.tagDetailTechTypes);

        showIntentDetails(false);
        showTagDetails(false);
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

        nfcReaderCallbackSupport = NfcReaderCallbackSupport.newBuilder().withActivity(this).withExecutor(threadPoolExecutor).withReaderCallbackDelegate(this).withPresenceCheckDelay(100).build();
        nfcReaderCallbackSupport.setNfcReaderMode(true);
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

            setTagDetails(tag);
            setIntentDetails(intent);
        });
    }

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("Tag discovered in activity");
        runOnUiThread(() -> {
            setTagPresent(true);

            setTagDetails(tag);
            setIntentDetails(intent);
        });
    }

    @Override
    public void onExternalTagLost(Intent intent) {
        runOnUiThread(() -> {
            setTagPresent(false);

            showIntentDetails(false);
            showTagDetails(false);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        externalNfcTagCallbackSupport.onPause();
        externalNfcServiceCallbackSupport.onPause();
        externalNfcReaderCallbackSupport.onPause();
        externalNfcTagLostCallbackSupport.onPause();

        nfcReaderCallbackSupport.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        externalNfcTagCallbackSupport.onResume();
        externalNfcReaderCallbackSupport.onResume();
        externalNfcTagLostCallbackSupport.onResume();
        externalNfcServiceCallbackSupport.onResume();

        nfcReaderCallbackSupport.onResume();

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

    public void setIntentDetails(Intent intent) {
        if(intent != null) {
            showIntentDetails(true);

            String action = intent.getAction();
            if(action != null) {
                intentDetailAction.setText(action.substring(action.lastIndexOf('.') + 1));
            } else {
                intentDetailAction.setText("-");
            }

            if(intent.hasExtra(NfcAdapter.EXTRA_ID)) {
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                intentDetailUuid.setText(ByteArrayHexStringConverter.byteArrayToHexString(id));
            } else {
                intentDetailUuid.setText("-");
            }

        } else {
            showIntentDetails(false);
        }
    }

    public void showIntentDetails(final boolean present) {
        if (present) {
            intentDetailsTable.setVisibility(View.VISIBLE);
            intentDetailsTitle.setVisibility(View.VISIBLE);
        } else {
            intentDetailsTable.setVisibility(View.GONE);
            intentDetailsTitle.setVisibility(View.GONE);
        }
    }

    public void showTagDetails(final boolean present) {
        if (present) {
            tagDetailsTable.setVisibility(View.VISIBLE);
            tagDetailsTitle.setVisibility(View.VISIBLE);
        } else {
            tagDetailsTable.setVisibility(View.GONE);
            tagDetailsTitle.setVisibility(View.GONE);
        }
    }

    public void setTagDetails(Tag tag) {
        if(tag != null) {
            showTagDetails(true);

            setTagDetailTechTypes(tag);
        } else {
            showTagDetails(false);
        }
    }

    public void setTagDetailTechTypes(Tag tag) {
        StringBuilder builder = new StringBuilder();

        String[] techList = tag.getTechList();
        for(int i = 0; i < techList.length; i++) {
            if(i > 0) {
                builder.append(", ");
            }
            String tech = techList[i];

            builder.append(tech.substring(tech.lastIndexOf('.') + 1));
        }

        setTextViewText(R.id.tagDetailTechTypes, builder);
    }

    public void setTextViewText(final int resource, final int string) {
        setTextViewText(resource, getString(string));
    }

    public void setTextViewText(final int resource, final CharSequence string) {
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