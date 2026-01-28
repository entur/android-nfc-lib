package no.entur.abt.nfc.example;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import no.entur.android.nfc.NfcReaderCallbackSupport;
import no.entur.android.nfc.detect.NfcTargetAnalyzeResult;
import no.entur.android.nfc.detect.NfcTargetAnalyzer;
import no.entur.android.nfc.detect.app.DefaultSelectApplicationAnalyzer;
import no.entur.android.nfc.detect.app.DesfireNativeSelectApplicationAnalyzer;
import no.entur.android.nfc.detect.app.DesfireSelectApplicationAnalyzer;
import no.entur.android.nfc.detect.app.EmvSelectApplicationAnalyzer;
import no.entur.android.nfc.detect.technology.DesfireEv1TechnologyAnalyzer;
import no.entur.android.nfc.detect.technology.IsodepTechnologyAnalyzer;
import no.entur.android.nfc.detect.technology.MifareUltralightTechnologyAnalyzer;
import no.entur.android.nfc.detect.uid.AnyLengthUidAnalyzer;
import no.entur.android.nfc.detect.uid.SevenByteNxpUidAnalyzer;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallbackSupport;
import no.entur.android.nfc.external.ExternalNfcTagLostCallback;
import no.entur.android.nfc.external.ExternalNfcTagLostCallbackSupport;
import no.entur.android.nfc.external.acs.reader.Acr1252UReader;
import no.entur.android.nfc.external.acs.reader.AcrAutomaticPICCPolling;
import no.entur.android.nfc.external.acs.reader.AcrPICC;
import no.entur.android.nfc.external.acs.reader.AcrReader;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.abt.nfc.example.utils.ParcelableExtraUtils;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.tech.IsoDep;
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
    private TextView tagDetailIdentify;

    private TextView tagDetailReaderValue;

    private NfcTargetAnalyzer nfcTargetAnalyzer;
    private boolean tagPresent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        tagDetailTechTypes = findViewById(R.id.tagDetailTechTypes);

        tagDetailReaderValue = findViewById(R.id.tagDetailReaderValue);

        tagDetailIdentify =  findViewById(R.id.tagDetailIdentifyValue);

        showIntentDetails(false);
        showTagDetails(false);
    }


    private void setupNfc() {
        MainApplication mainApplication = (MainApplication) getApplication();
        this.threadPoolExecutor = mainApplication.getThreadPoolExecutor();

        boolean receiverExported = true;

        externalNfcTagCallbackSupport = new ExternalNfcTagCallbackSupport(this, this, threadPoolExecutor, receiverExported);
        externalNfcTagCallbackSupport.setEnabled(true);

        externalNfcServiceCallbackSupport = new ExternalNfcServiceCallbackSupport(this, this, threadPoolExecutor, receiverExported);
        externalNfcServiceCallbackSupport.setEnabled(true);

        externalNfcReaderCallbackSupport = new ExternalNfcReaderCallbackSupport(this, this, threadPoolExecutor, receiverExported);
        externalNfcReaderCallbackSupport.setEnabled(true);

        externalNfcTagLostCallbackSupport = new ExternalNfcTagLostCallbackSupport(this, this, threadPoolExecutor, receiverExported);
        externalNfcTagLostCallbackSupport.setEnabled(true);

        nfcReaderCallbackSupport = NfcReaderCallbackSupport.newBuilder().withActivity(this).withExecutor(threadPoolExecutor).withReaderCallbackDelegate(this).withPresenceCheckDelay(100).withReceiverExported(receiverExported).build();
        nfcReaderCallbackSupport.setNfcReaderMode(true);

        setupTagAnalyzer();
    }

    private void setupTagAnalyzer() {


        this.nfcTargetAnalyzer = NfcTargetAnalyzer.newBuilder()
            .add( (c) -> {
                c
                    .withId("ultralight")
                    .withTechnologyAnalyzer(new MifareUltralightTechnologyAnalyzer(MifareUltralight.TYPE_ULTRALIGHT, "ultralight"))
                    .withUidAnalyzer(new SevenByteNxpUidAnalyzer());
            })
            .add( (c) -> {
                c
                    .withId("NOD travelcard")
                    .withTechnologyAnalyzer(new DesfireEv1TechnologyAnalyzer())
                    .withUidAnalyzer(new SevenByteNxpUidAnalyzer())
                    .withSelectApplicationAnalyzer(new DesfireNativeSelectApplicationAnalyzer(new byte[] { (byte) 0x00, (byte) 0x80, (byte) 0x57 }))
                ;
            })
/*
                .add( (c) -> {
                    c
                            .withId("NOD 1")
                            .withTechnologyAnalyzer(new IsodepTechnologyAnalyzer())
                            .withUidAnalyzer(new AnyLengthUidAnalyzer())
                            .withSelectApplicationAnalyzer(new DesfireSelectApplicationAnalyzer(ByteArrayHexStringConverter.hexStringToByteArray("008057")));
                    ;
                })
                .add( (c) -> {
                    c
                            .withId("NOD 2")
                            .withTechnologyAnalyzer(new IsodepTechnologyAnalyzer())
                            .withUidAnalyzer(new AnyLengthUidAnalyzer())
                            .withSelectApplicationAnalyzer(new DefaultSelectApplicationAnalyzer(ByteArrayHexStringConverter.hexStringToByteArray("578000")));
                    ;
                })
*/
                .add( (c) -> {
                c
                    .withId("HCE app")
                    .withTechnologyAnalyzer(new IsodepTechnologyAnalyzer())
                    .withUidAnalyzer(new AnyLengthUidAnalyzer())
                    .withSelectApplicationAnalyzer(new DefaultSelectApplicationAnalyzer(ByteArrayHexStringConverter.hexStringToByteArray("D2760000850101")));
                ;
            })
            .add( (c) -> {
                c
                    .withId("MTS7")
                    .withTechnologyAnalyzer(new IsodepTechnologyAnalyzer())
                    .withUidAnalyzer(new AnyLengthUidAnalyzer())
                    .withSelectApplicationAnalyzer(new DefaultSelectApplicationAnalyzer(new byte[]{(byte) 0xA0, 0x00, 0x00, 0x07, (byte) 0x81}));
                ;
            })

            .add( (c) -> {
                c
                    .withId("EMV card")
                    .withTechnologyAnalyzer(new IsodepTechnologyAnalyzer())
                    .withUidAnalyzer(new AnyLengthUidAnalyzer())
                    .withSelectApplicationAnalyzer(new EmvSelectApplicationAnalyzer())
                ;
            })
                .build();

        nfcTargetAnalyzer.enableAll();
    }

    @Override
    public void onExternalNfcReaderOpened(Intent intent) {
        if (intent.hasExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL)) {
            Parcelable reader = ParcelableExtraUtils.getParcelableExtra(intent, ExternalNfcReaderCallback.EXTRA_READER_CONTROL, Parcelable.class);

            LOGGER.info("Got reader type " + reader.getClass().getName() + " in activity");

            // example: attempt to talk to a SAM on ACR 1252
            if(reader instanceof AcrReader) {
                AcrReader acrReader = (AcrReader)reader;

                acrReader.setPICC(AcrPICC.POLL_ISO14443_TYPE_A, AcrPICC.POLL_ISO14443_TYPE_B);

                if (acrReader.getName().contains("1252")) {
                    Acr1252UReader acr1252UReader = (Acr1252UReader) acrReader;
                    acr1252UReader.setAutomaticPICCPolling(AcrAutomaticPICCPolling.AUTO_PICC_POLLING, AcrAutomaticPICCPolling.ACTIVATE_PICC_WHEN_DETECTED);

                    if(acrReader.getNumberOfSlots() == 2) {
                        try {
                            byte[] power = acrReader.power(1, 2);
                            LOGGER.info("Got power response " + ByteArrayHexStringConverter.toHexString(power));

                            acrReader.setProtocol(1, 1);

                            // try random command, expect response code 6986
                            byte[] transmit = acrReader.transmit(1, new byte[]{0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x41, 0x00});

                            LOGGER.info("Got reader response " + ByteArrayHexStringConverter.toHexString(transmit));
                        } catch (Exception e) {
                            LOGGER.error("Problem talking to SAM", e);
                        }
                    }
                }
            }
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

    public boolean isTagPresent() {
        return tagPresent;
    }

    @Override
    public void onExternalTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("External Tag discovered in activity");

        tagPresent = true;

        runOnUiThread(() -> {
            setTagPresent(true);

            setTagDetails(tag, intent);
            setIntentDetails(intent);
        });
        threadPoolExecutor.submit(() -> {
            try {
                if(tag != null) {
                    setTagDetailsIdentify(tag, intent);
                    setContents(tag, intent);
                }
            } catch(Exception e) {
                LOGGER.warn("Problem processing tag technology", e);
            }
        });
    }

    @Override
    public void onTagDiscovered(Tag tag, Intent intent) {
        LOGGER.info("Tag discovered in activity");

        tagPresent = true;

        runOnUiThread(() -> {
            setTagPresent(true);

            setTagDetails(tag, intent);
            setIntentDetails(intent);
        });
        threadPoolExecutor.submit(() -> {
            try {
                if(tag != null) {
                    setTagDetailsIdentify(tag, intent);
                    setContents(tag, intent);
                }
            } catch(Exception e) {
                LOGGER.warn("Problem processing tag technology", e);
            }
        });
    }

    @Override
    public void onExternalTagLost(Intent intent) {
        tagPresent = false;

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

    public void setTagDetails(Tag tag, Intent intent) {
        if(tag != null) {
            showTagDetails(true);

            setTagDetailTechTypes(tag);

            String readerId = "-";
            if(intent.hasExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID)) {
                readerId = intent.getStringExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID);
            }

            tagDetailReaderValue.setText(readerId);
        } else {
            showTagDetails(false);

            tagDetailIdentify.setText("");
        }
    }

    private void setTagDetailsIdentify(Tag tag, Intent intent) throws IOException {
        List<NfcTargetAnalyzeResult> results = nfcTargetAnalyzer.analyze(tag, intent);

        if(results.isEmpty()) {
            LOGGER.info("Empty analyzer result");
            runOnUiThread(() -> {
                tagDetailIdentify.setText(R.string.tagDetailIdentifyUnknown);
            });
        } else {
            LOGGER.info("Got " + results.size() + " analyzer results");
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < results.size(); i++) {
                NfcTargetAnalyzeResult result = results.get(i);
                if(i > 0) {
                    builder.append(", ");
                }
                builder.append(result.getId());
            }
            LOGGER.info(builder.toString());

            runOnUiThread(() -> {
                tagDetailIdentify.setText(builder.toString());
            });
        }

    }

    private void setContents(Tag tag, Intent intent) throws IOException {

        if(isTechType(tag, android.nfc.tech.IsoDep.class.getName())) {

            IsoDep isoDep = IsoDep.get(tag);

            LOGGER.info("Hi-layer response: " + ByteArrayHexStringConverter.toHexString(isoDep.getHiLayerResponse()));
            LOGGER.info("Historical bytes: " + ByteArrayHexStringConverter.toHexString(isoDep.getHistoricalBytes()));
            LOGGER.info("Timeout: " + isoDep.getTimeout());
            LOGGER.info("Max transceive length: " + isoDep.getMaxTransceiveLength());

            byte[] wrapped = new byte[] { 0x5A, (byte) 0x11, (byte) 0x81, (byte) 0x57 };

            isoDep.transceive(wrapped);
        } else if(isTechType(tag, android.nfc.tech.MifareUltralight.class.getName())) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            if(intent != null && intent.hasExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE)) {
                // handle NTAG21x types
                // the NTAG21x product familiy have replacements for all previous Ultralight tags
                int type = intent.getIntExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE, 0);

                NfcA nfcA = NfcA.get(tag);
                if(nfcA == null) {
                    throw new IllegalArgumentException("No NfcA");
                }
                LOGGER.info("Timeout: " + nfcA.getTimeout());
                LOGGER.info("Max transceive length: " + nfcA.getMaxTransceiveLength());

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

                LOGGER.info("Timeout: " + mifareUltralight.getTimeout());
                LOGGER.info("Max transceive length: " + mifareUltralight.getMaxTransceiveLength());

                int length;

                int type = mifareUltralight.getType();
                switch (type) {
                    case MifareUltralight.TYPE_ULTRALIGHT: {
                        length = 12 + 4;

                        break;
                    }
                    case MifareUltralight.TYPE_ULTRALIGHT_C: {
                        length = 36 + 4;

                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown mifare ultralight tag type " + type);
                }

                // android read 4 and 4 pages of 4 bytes
                for (int i = 0; i < length; i += 4) {
                    Log.d(getClass().getName(), "Read frame " + i + " -> " + (i + 4) + " (exclusive)");
                    bout.write(mifareUltralight.readPages(i));
                }

                mifareUltralight.close();

                LOGGER.info("Tag id is " + ByteArrayHexStringConverter.toHexString(mifareUltralight.getTag().getId()));
            }

            byte[] buffer = bout.toByteArray();

            StringBuilder builder = new StringBuilder();
            for(int k = 0; k < buffer.length; k+= 4) {
                builder.append( String.format("%02x", (k / 4)) + " " + ByteArrayHexStringConverter.toHexString(buffer, k, 4));
                builder.append('\n');
            }

            LOGGER.info(builder.toString());
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

    public void stopHidReaderService(View view) {
        LOGGER.info("Stop Hid reader service");

        mainApplication.setExternalHidNfcReader(false);
    }

    public void startHidReaderService(View view) {
        LOGGER.info("Start Hid reader service");

        mainApplication.setExternalHidNfcReader(true);
    }


    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}