package no.entur.abt.nfc.example;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
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
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.NfcA;

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
            setContents(tag, intent);
        });
    }

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("Tag discovered in activity");
        runOnUiThread(() -> {
            setTagPresent(true);

            setTagDetails(tag);
            setIntentDetails(intent);
            setContents(tag, intent);
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

    private void setContents(Tag tag, Intent intent) {

        if(isTechType(tag, android.nfc.tech.MifareUltralight.class.getName())) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                if(intent != null && intent.hasExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE)) {
                    // handle NTAG21x types
                    // the NTAG21x product familiy have replacements for all previous Ultralight tags
                    int type = intent.getIntExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE, 0);

                    NfcA nfcA = NfcA.get(tag);
                    if(nfcA == null) {
                        throw new IllegalArgumentException("No NTAG");
                    }

                    int size;
                    switch(type) {
                        case NfcNtagVersion.TYPE_NTAG210: {
                            size = 48;
                            break;
                        }
                        case NfcNtagVersion.TYPE_NTAG212: {
                            size = 128;
                            break;
                        }
                        case NfcNtagVersion.TYPE_NTAG213: {
                            size = 144;
                            break;
                        }
                        case NfcNtagVersion.TYPE_NTAG215: {
                            size = 504;
                            break;
                        }
                        case NfcNtagVersion.TYPE_NTAG216 :
                        case NfcNtagVersion.TYPE_NTAG216F : {
                            size = 888;
                            break;
                        }
                        default : {
                            size = 48;
                        }
                    }
                    int pagesToRead = size / 4 + 4;

                    // instead of reading 4 and 4 pages, read more using the FAST READ command
                    int pagesPerRead;
                    if(nfcA.getMaxTransceiveLength() > 0) {
                        pagesPerRead = Math.min(255, nfcA.getMaxTransceiveLength() / 4);
                    } else {
                        pagesPerRead = 255;
                    }

                    int reads = pagesToRead / pagesPerRead;

                    if(pagesToRead % pagesPerRead != 0) {
                        reads++;
                    }

                    try {
                        nfcA.connect();
                        int read = 0;
                        for (int i = 0; i < reads; i++) {
                            int range = Math.min(pagesPerRead, pagesToRead - read);

                            byte[] fastRead = new byte[]{
                                    0x3A,
                                    (byte) (read & 0xFF), // start page
                                    (byte) ((read + range - 1) & 0xFF), // end page (inclusive)
                            };

                            bout.write(nfcA.transceive(fastRead));

                            read += range;
                        }
                    } finally {
                        nfcA.close();
                    }


                } else {
                    MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                    if(mifareUltralight == null) {
                        throw new IllegalArgumentException("No Mifare Ultralight");
                    }
                    mifareUltralight.connect();

                    int length;

                    int type = mifareUltralight.getType();
                    switch (type) {
                        case MifareUltralight.TYPE_ULTRALIGHT: {
                            length = 12;

                            break;
                        }
                        case MifareUltralight.TYPE_ULTRALIGHT_C: {
                            length = 36;

                            break;
                        }
                        default:
                            throw new IllegalArgumentException("Unknown mifare ultralight tag " + type);
                    }

                    // android read 4 and 4 pages of 4 bytes
                    for (int i = 0; i < length; i += 4) {
                        bout.write(mifareUltralight.readPages(i));
                    }
                    mifareUltralight.close();
                }

                byte[] buffer = bout.toByteArray();

                StringBuilder builder = new StringBuilder();
                for(int k = 0; k < buffer.length; k+= 4) {
                    builder.append( String.format("%02x", (k / 4)) + " " + ByteArrayHexStringConverter.toHexString(buffer, k, 4));
                    builder.append('\n');
                }

                LOGGER.info(builder.toString());
            } catch(Exception e) {
                LOGGER.warn("Problem processing tag technology", e);
            }
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

    private boolean isTechType(Tag tag, String type) {
        String[] techList = tag.getTechList();
        for (String t : techList) {
            if(t.equals(type)) {
                return true;
            }
        }

        return false;
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