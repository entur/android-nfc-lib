package no.entur.android.nfc.external.hid.reader;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import no.entur.android.nfc.external.ExternalBarcodeCallback;
import no.entur.android.nfc.external.hid.HidMqttService;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.hid.Atr210MqttHandler;
import no.entur.android.nfc.external.hid.card.Atr210CardContext;
import no.entur.android.nfc.external.hid.card.Atr210CardService;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.intent.Atr210Reader;
import no.entur.android.nfc.external.hid.intent.bind.Atr210ReaderBinder;
import no.entur.android.nfc.external.hid.intent.bind.Atr210ReaderTechnology;
import no.entur.android.nfc.external.hid.intent.command.Atr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReaderStatus;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;
import no.entur.android.nfc.external.hid.intent.NfcCardStatus;
import no.entur.android.nfc.external.hid.dto.atr210.TicketRequest;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

import org.nfctools.api.ATR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Atr210ReaderService implements SynchronizedRequestMessageListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderService.class);

    private static final long HEARTBEAT_LEEWAY = 3000;
    private final INFcTagBinder infcTagBinder;

    protected Atr210ReaderContext readerContext;

    protected Atr210ReaderCommands readerCommands;

    protected Atr210CardService atr210CardService;

    protected Context context;

    protected SynchronizedRequestResponseMessages<String> requestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final MqttServiceClient mqttClient;

    protected Atr210Reader atr210Reader;

    protected final TagProxyStore tagProxyStore;

    protected long nextHeartbeatDeadline = -1L;

    public Atr210ReaderService(Context context, MqttServiceClient mqttClient, Atr210ReaderContext readerContext, long transcieveTimeout, TagProxyStore tagProxyStore) {
        this.context = context;
        this.mqttClient = mqttClient;
        this.readerContext = readerContext;

        this.readerCommands = Atr210ReaderCommands.newBuilder()
                .withReaderContext(readerContext)
                .withSynchronizedRequestResponseMessages(requestResponseMessages)
                .withMqttClient(mqttClient)
                .build();

        this.tagProxyStore = tagProxyStore;

        Atr210ReaderBinder binder = new Atr210ReaderBinder();
        binder.setReaderCommandsWrapper(new Atr210ReaderCommandsWrapper(readerCommands));
        this.atr210Reader = new Atr210Reader("ATR210", readerContext.getClientId(), readerContext.getProviderId(), binder);

        infcTagBinder = new INFcTagBinder(tagProxyStore);
        infcTagBinder.setReaderTechnology(new Atr210ReaderTechnology(true));

        this.atr210CardService = new Atr210CardService(context, requestResponseMessages, mqttClient, transcieveTimeout, infcTagBinder, tagProxyStore);

        requestResponseMessages.setRequestMessageListener(this);
    }

    public void open() {
        subscribe();

        broadcastOpened();
    }

    public void close() {
        try {
            unsubscribe();
        } finally {
            broadcastClosed();
        }
    }

    public void subscribe() {

        // correlation of messages (i.e. request-response) is primarily "first message on topic".

        // txpt/ticketreader/{PROVIDER_ID}/request/validation'
        // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration

        // List of readers response:
        // txpt/ticketreader/{PROVIDER_ID}/nfc/readers

        String prefix = "itxpt/ticketreader/" + readerContext.getProviderId();

        mqttClient.subscribeToJson(prefix + "/request/validation", this::barcode, TicketRequest.class);
        mqttClient.subscribeToJson(prefix + "/nfc/readers/configuration", this::configuration, NfcConfiguationResponse.class);
        mqttClient.subscribeToJson(prefix + "/nfc/readers", this::reader, ReadersStatusResponse.class);

        mqttClient.subscribeToJson(prefix + "/nfc/hf/apdu/response", this::onAdpuResponse, NfcAdpuTransmitResponse.class);

        // this is invoked on a new card, without there being a corresponding message sent to
        // txpt/ticketreader/{PROVIDER_ID}/nfc/readers/status/request first. Documentation is not accurate.
        mqttClient.subscribeToJson(prefix+ "/nfc/readers/status", this::newTagStatus, ReadersStatusResponse.class);
    }

    public void unsubscribe() {
        String prefix = "itxpt/ticketreader/" + readerContext.getProviderId();

        mqttClient.unsubscribe(prefix + "/request/validation");
        mqttClient.unsubscribe(prefix + "/nfc/readers/configuration");
        mqttClient.unsubscribe(prefix + "/nfc/readers");

        mqttClient.unsubscribe(prefix + "/nfc/hf/apdu/response");

        mqttClient.unsubscribe(prefix + "/nfc/readers/status");
    }

    public void broadcastClosed() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

        if(atr210Reader != null) {
            intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, atr210Reader.getId());
        }

        context.sendBroadcast(intent, Atr210MqttHandler.ANDROID_PERMISSION_NFC);
    }

    public void broadcastOpened() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);

        intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, atr210Reader);

        context.sendBroadcast(intent, Atr210MqttHandler.ANDROID_PERMISSION_NFC);
    }

    private void reader(String topic, ReadersStatusResponse readersStatusResponse) {
        requestResponseMessages.onResponseMessage(new Atr210FirstMessageOnTopicSynchronizedResponseMessage(readersStatusResponse, topic));
    }

    private void configuration(String topic, NfcConfiguationResponse nfcConfiguationResponse) {
        requestResponseMessages.onResponseMessage(new Atr210FirstMessageOnTopicSynchronizedResponseMessage(nfcConfiguationResponse, topic));
    }

    public void onAdpuResponse(String topic, NfcAdpuTransmitResponse response) {
        Atr210CardService card = this.atr210CardService;
        if(card != null) {
            card.onAdpuResponse(topic, response);
        } else {
            LOGGER.info("Not forwarding ADPU response for reader {}; no card", readerContext.getClientId());
        }
    }

    public void newTagStatus(ReadersStatusResponse response) {
        List<ReaderStatus> hfReaders = response.getHfReaders();

        // expect
        // CHANGED,PRESENT
        // CHANGED,PRESENT,INUSE
        // and possibly back to CHANGED,PRESENT
        // then
        // CHANGED,EMPTY

        for (ReaderStatus hfReader : hfReaders) {
            if(isTagPresent(hfReader)) {

                if(!atr210CardService.hasCardContext()) {
                    Atr210CardContext context = new Atr210CardContext();
                    context.setClientId(readerContext.getClientId());
                    context.setProviderId(readerContext.getProviderId());

                    if (hfReader.hasCardAtr()) {
                        context.setAtr(ByteArrayHexStringConverter.hexStringToByteArray(hfReader.getCardATR()));

                        ATR atr = new ATR(context.getAtr());

                        context.setHistoricalBytes(atr.getHistoricalBytes());
                    }
                    if (hfReader.hasCardCsn()) {
                        context.setUid(ByteArrayHexStringConverter.hexStringToByteArray(hfReader.getCardCSN()));
                    }

                    atr210CardService.setCardContext(context);
                    atr210CardService.createTag();
                }
            } else if(isTagLost(hfReader)) {
                atr210CardService.onTagLost();
            }
        }
    }

    private boolean isTagPresent(ReaderStatus hfReader) {
        // CHANGED,PRESENT
        return hfReader.hasStatus(NfcCardStatus.CHANGED) && hfReader.hasStatus(NfcCardStatus.PRESENT);
    }

    private boolean isTagLost(ReaderStatus hfReader) {
        // CHANGED,PRESENT
        return hfReader.hasStatus(NfcCardStatus.CHANGED) && hfReader.hasStatus(NfcCardStatus.EMPTY);
    }

    public boolean hasHeartbeat() {
        return nextHeartbeatDeadline + HEARTBEAT_LEEWAY >= System.currentTimeMillis();
    }

    public void setNextHeartbeatDeadline(long nextHeartbeatDeadline) {
        this.nextHeartbeatDeadline = nextHeartbeatDeadline;
    }

    protected void barcode(TicketRequest ticketRequest) {
        try {
            Intent intent = new Intent(ExternalBarcodeCallback.ACTION_BARCODE_DISCOVERED);

            String barcode = ticketRequest.getBarcode();

            byte[] bytes = Base64.decode(barcode, Base64.NO_WRAP);

            intent.putExtra(ExternalBarcodeCallback.BARCODE_EXTRA_BODY, bytes);
            intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, atr210Reader.getId());

            context.sendBroadcast(intent, Atr210MqttHandler.ANDROID_PERMISSION_NFC);
        } catch (Exception e) {
            LOGGER.warn("Problem handling barcode message", e);
        }
    }

    public void configureNfcReaders(boolean hf, boolean sam, long timeout) throws IOException {

        // enable or disable readers

        // send configuration request

        // if hf id none, sam id none or enabled false:

        // get list of NFC and SAM devices

        // update configuration

        // check response received

        NfcConfiguationResponse nfcReadersConfiguration = readerCommands.getNfcReadersConfiguration(timeout);

        boolean enable = nfcReadersConfiguration.hasEnabled() && !nfcReadersConfiguration.getEnabled();

        boolean updateEnabled = (hf || sam) != enable;

        boolean updateHf = hf == "none".equals(nfcReadersConfiguration.getHfId());
        boolean updateSam = sam == "none".equals(nfcReadersConfiguration.getSamId());

        if(updateEnabled || updateHf || updateSam) {
            NfcConfiguationRequest request = new NfcConfiguationRequest();

            ReadersStatusResponse nfcReaders = readerCommands.getNfcReaders(timeout);

            if(hf) {
                if(nfcReaders.hasHfReaders()) {
                    // use the first
                    ReaderStatus readerStatus = nfcReaders.getHfReaders().get(0);
                    request.setHfId(readerStatus.getId());
                } else {
                    LOGGER.warn("Unable to enable NFC HF reader; no readers present.");
                }
            } else {
                request.setHfId("none");
            }

            if(sam) {
                if (nfcReaders.hasSamReaders()) {
                    ReaderStatus readerStatus = nfcReaders.getSamReaders().get(0);
                    request.setSamId(readerStatus.getId());
                } else {
                    LOGGER.warn("Unable to enable NFC SAM reader; no readers present.");
                }
            } else {
                request.setSamId("none");
            }

            request.setEnabled(hf || sam);

            NfcConfiguationResponse nfcConfiguationResponse = readerCommands.setNfcReadersConfiguration(request, timeout);

            // verify
            if(!Objects.equals(request.getEnabled(), nfcConfiguationResponse.getEnabled())
                    || !Objects.equals(request.getHfId(), nfcConfiguationResponse.getHfId())
                    || !Objects.equals(request.getSamId(), nfcConfiguationResponse.getSamId())
            ) {
                LOGGER.warn("Configuration update problem, wanted {}, got {}.", request, nfcReadersConfiguration);
            } else {
                LOGGER.warn("Configuration updated");
            }
        } else {
            LOGGER.info("Reader configuration already up to date (enabled {}, HF {}, SAM {})", enable, nfcReadersConfiguration.getHfId(), nfcReadersConfiguration.getSamId());
        }

    }


    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<String> message, SynchronizedResponseMessageListener<String> listener) throws IOException {
        mqttClient.publishAsJson(message.getTopic(), message.getPayload());
    }
}
