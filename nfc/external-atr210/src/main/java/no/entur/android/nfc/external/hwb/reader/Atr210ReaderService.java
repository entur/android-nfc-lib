package no.entur.android.nfc.external.hwb.reader;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.mqtt3.client.MqttServiceClient;
import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.hwb.HwbMqttClient;
import no.entur.android.nfc.external.hwb.Atr210MqttService;
import no.entur.android.nfc.external.hwb.card.Atr210CardService;
import no.entur.android.nfc.external.hwb.card.Atr210CardContext;
import no.entur.android.nfc.external.hwb.intent.DefaultAtr210Reader;
import no.entur.android.nfc.external.hwb.intent.Atr210Reader;
import no.entur.android.nfc.external.hwb.intent.bind.DefaultAtr210ReaderBinder;
import no.entur.android.nfc.external.hwb.intent.bind.Atr210ReaderTechnology;
import no.entur.android.nfc.external.hwb.intent.command.DefaultAtr210ReaderCommandsWrapper;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Atr210ReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderService.class);
    private final INFcTagBinder infcTagBinder;

    protected Atr210ReaderContext readerContext;

    protected Atr210ReaderCommands readerCommands;

    protected Atr210CardService atr210CardService;

    protected Context context;

    protected SynchronizedRequestResponseMessages<String> diagnosticsRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final MqttServiceClient hwbMqttClient;

    protected Atr210Reader atr210Reader;

    protected final TagProxyStore tagProxyStore;

    public Atr210ReaderService(Context context, MqttServiceClient hwbMqttClient, SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, Atr210ReaderContext readerContext, long transcieveTimeout, TagProxyStore tagProxyStore) {
        this.context = context;
        this.hwbMqttClient = hwbMqttClient;
        this.readerContext = readerContext;

        this.readerCommands = Atr210ReaderCommands.newBuilder()
                .withReaderContext(readerContext)
                .withReaderMessageConverter(new Atr210ReaderMessageConverter())
                .withSynchronizedRequestResponseMessages(diagnosticsRequestResponseMessages)
                .build();

        this.tagProxyStore = tagProxyStore;

        // TODO pass type via reader context? what if not available?
        DefaultAtr210ReaderBinder binder = new DefaultAtr210ReaderBinder();
        binder.setReaderCommandsWrapper(new DefaultAtr210ReaderCommandsWrapper(readerCommands));
        this.atr210Reader = new DefaultAtr210Reader("HWB", binder);

        infcTagBinder = new INFcTagBinder(tagProxyStore);
        infcTagBinder.setReaderTechnology(new Atr210ReaderTechnology(true));

        this.atr210CardService = new Atr210CardService(context, adpuRequestResponseMessages, hwbMqttClient, transcieveTimeout, infcTagBinder, tagProxyStore);
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
        hwbMqttClient.subscribe("/device/" + readerContext.getDeviceId() + "/diagnostics", DiagnosticsSchema.class, this::diagnostics);
    }

    private void unsubscribe() {
        hwbMqttClient.unsubscribe("/device/" + readerContext.getDeviceId() + "/diagnostics");
    }

    public void broadcastClosed() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

        context.sendBroadcast(intent, Atr210MqttService.ANDROID_PERMISSION_NFC);
    }

    public void broadcastOpened() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);

        intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, atr210Reader);

        context.sendBroadcast(intent, Atr210MqttService.ANDROID_PERMISSION_NFC);
    }

    protected void diagnostics(DiagnosticsSchema diagnosticsSchema) {
        try {
            diagnosticsRequestResponseMessages.onResponseMessage(new Atr210ReaderPresentSynchronizedResponseMessage(diagnosticsSchema, true));
        } catch (Exception e) {
            LOGGER.warn("Problem handling diagnostics message", e);
        }
    }

    public void onAdpuResponse(ReceiveSchema receiveSchema) {
        Atr210CardService card = this.atr210CardService;
        if(card != null) {
            card.onAdpuResponse(receiveSchema);
        } else {
            LOGGER.info("Not forwarding ADPU response for reader {}; no card", readerContext.getDeviceId());
        }
    }

    public void newTag(NfcSchema schema) {
        List<CardContent> cardContent = schema.getCardContent();

        String token = schema.getToken();

        String travelCardNumber = schema.getTravelCardNumber();

        Atr210CardContext context = new Atr210CardContext();
        context.setDeviceId(readerContext.getDeviceId());

        // is this a desfire card? if so then desfire native commands
        if(isDesfire(schema)) {
            context.setApduType(TransmitSchema.ApduType.DESFIRE);
            context.setHistoricalBytes(new byte[]{(byte) 0x80});
        } else {
            context.setApduType(TransmitSchema.ApduType.ISO_7816);
            context.setHistoricalBytes(new byte[]{});
        }

        context.setTraceId(schema.getTraceId());
        // TODO set transcieve timeout?
        //context.setTranscieveTimeout();

        atr210CardService.setCardContext(context);
        atr210CardService.createTag(travelCardNumber, token, cardContent);
    }

    private boolean isDesfire(NfcSchema schema) {
        if(schema.getTravelCardNumber() != null) {
            return true;
        }
        if(schema.getCardContent() != null && !schema.getCardContent().isEmpty()) {
           return true;
        }
        return false;
    }

    public boolean isPresent(long timeout) throws IOException {
        return readerCommands.isPresent(timeout);
    }

}
