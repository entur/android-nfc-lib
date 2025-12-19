package no.entur.android.nfc.external.hid.reader;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.hid.Atr210MqttHandler;
import no.entur.android.nfc.external.hid.card.Atr210CardContext;
import no.entur.android.nfc.external.hid.card.Atr210CardService;
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
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

import org.nfctools.api.ATR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Atr210ReaderService {

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
                .build();

        this.tagProxyStore = tagProxyStore;

        Atr210ReaderBinder binder = new Atr210ReaderBinder();
        binder.setReaderCommandsWrapper(new Atr210ReaderCommandsWrapper(readerCommands));
        this.atr210Reader = new Atr210Reader("ATR210", readerContext.getClientId(), readerContext.getProviderId(), binder);

        infcTagBinder = new INFcTagBinder(tagProxyStore);
        infcTagBinder.setReaderTechnology(new Atr210ReaderTechnology(true));

        this.atr210CardService = new Atr210CardService(context, requestResponseMessages, mqttClient, transcieveTimeout, infcTagBinder, tagProxyStore);
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

    private void subscribe() {

        // correlation of messages (i.e. request-response) is primarily "first message on topic".

        // txpt/ticketreader/{PROVIDER_ID}/request/validation'
        // itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration

        // List of readers response:
        // txpt/ticketreader/{PROVIDER_ID}/nfc/readers
        mqttClient.subscribeToJson("txpt/ticketreader/" + readerContext.getProviderId() + "/request/validation", this::barcode, TicketRequest.class);
        mqttClient.subscribeToJson("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/configuration", this::configuration, NfcConfiguationResponse.class);
        mqttClient.subscribeToJson("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers", this::reader, ReadersStatusResponse.class);

        mqttClient.subscribeToJson("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/hf/apdu/response", this::onAdpuResponse, NfcAdpuTransmitResponse.class);

        // this is invoked on a new card, without there being a corresponding message sent to
        // txpt/ticketreader/{PROVIDER_ID}/nfc/readers/status/request first. Documentation is not accurate.
        mqttClient.subscribeToJson("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/status", this::newTag, ReadersStatusResponse.class);
    }

    private void unsubscribe() {
        mqttClient.unsubscribe("txpt/ticketreader/" + readerContext.getProviderId() + "/request/validation");
        mqttClient.unsubscribe("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/configuration");
        mqttClient.unsubscribe("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers");

        mqttClient.unsubscribe("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/hf/apdu/response");

        mqttClient.unsubscribe("txpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/status");

    }

    public void broadcastClosed() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

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

    public void newTag(ReadersStatusResponse response) {
        List<ReaderStatus> hfReaders = response.getHfReaders();

        for (ReaderStatus hfReader : hfReaders) {
            if(isTagPresent(hfReader)) {
                Atr210CardContext context = new Atr210CardContext();
                context.setClientId(readerContext.getClientId());
                context.setProviderId(readerContext.getProviderId());

                if(hfReader.hasCardAtr()) {
                    context.setAtr(ByteArrayHexStringConverter.hexStringToByteArray(hfReader.getCardATR()));

                    ATR atr = new ATR(context.getAtr());

                    context.setHistoricalBytes(atr.getHistoricalBytes());

                }
                if(hfReader.hasCardCsn()) {
                    context.setUid(ByteArrayHexStringConverter.hexStringToByteArray(hfReader.getCardCSN()));
                }

                atr210CardService.setCardContext(context);
                atr210CardService.createTag();
                
            } else if(isTagLost(hfReader)) {
                atr210CardService.clearCardContext();
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
            // TODO
        } catch (Exception e) {
            LOGGER.warn("Problem handling diagnostics message", e);
        }
    }



}
