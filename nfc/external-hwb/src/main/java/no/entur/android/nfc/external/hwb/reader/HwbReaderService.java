package no.entur.android.nfc.external.hwb.reader;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcServiceCallback;
import no.entur.android.nfc.external.hwb.HwbMqttClient;
import no.entur.android.nfc.external.hwb.HwbMqttService;
import no.entur.android.nfc.external.hwb.card.HwbCardService;
import no.entur.android.nfc.external.hwb.card.HwbCardContext;
import no.entur.android.nfc.external.hwb.intent.DefaultHwbReader;
import no.entur.android.nfc.external.hwb.intent.HwbReader;
import no.entur.android.nfc.external.hwb.intent.HwbService;
import no.entur.android.nfc.external.hwb.intent.HwbTransceiveResultExceptionMapper;
import no.entur.android.nfc.external.hwb.intent.bind.DefaultHwbReaderBinder;
import no.entur.android.nfc.external.hwb.intent.bind.HwbReaderTechnology;
import no.entur.android.nfc.external.hwb.intent.bind.HwbServiceBinder;
import no.entur.android.nfc.external.hwb.intent.command.DefaultHwbReaderCommandsWrapper;
import no.entur.android.nfc.external.hwb.intent.command.HwbServiceCommandsWrapper;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.tag.IsoDepTagServiceSupport;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HwbReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbReaderService.class);
    private final INFcTagBinder infcTagBinder;

    protected HwbReaderContext readerContext;

    protected HwbReaderCommands readerCommands;

    protected HwbCardService hwbCardService;

    protected Context context;

    protected SynchronizedRequestResponseMessages<String> diagnosticsRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final HwbMqttClient hwbMqttClient;

    protected HwbReader hwbReader;

    protected final TagProxyStore tagProxyStore;

    public HwbReaderService(Context context, HwbMqttClient hwbMqttClient, SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, HwbReaderContext readerContext, long transcieveTimeout, TagProxyStore tagProxyStore) {
        this.context = context;
        this.hwbMqttClient = hwbMqttClient;
        this.readerContext = readerContext;

        this.readerCommands = HwbReaderCommands.newBuilder()
                .withReaderContext(readerContext)
                .withReaderMessageConverter(new HwbReaderMessageConverter())
                .withSynchronizedRequestResponseMessages(diagnosticsRequestResponseMessages)
                .build();

        this.tagProxyStore = tagProxyStore;

        // TODO pass type via reader context? what if not available?
        DefaultHwbReaderBinder binder = new DefaultHwbReaderBinder();
        binder.setReaderCommandsWrapper(new DefaultHwbReaderCommandsWrapper(readerCommands));
        this.hwbReader = new DefaultHwbReader("HWB", binder);

        infcTagBinder = new INFcTagBinder(tagProxyStore);
        infcTagBinder.setReaderTechnology(new HwbReaderTechnology());

        this.hwbCardService = new HwbCardService(context, adpuRequestResponseMessages, hwbMqttClient, transcieveTimeout, infcTagBinder, tagProxyStore);
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

        context.sendBroadcast(intent, HwbMqttService.ANDROID_PERMISSION_NFC);
    }

    public void broadcastOpened() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);

        intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, hwbReader);

        context.sendBroadcast(intent, HwbMqttService.ANDROID_PERMISSION_NFC);
    }

    protected void diagnostics(DiagnosticsSchema diagnosticsSchema) {
        try {
            diagnosticsRequestResponseMessages.onResponseMessage(new HwbReaderPresentSynchronizedResponseMessage(diagnosticsSchema, true));
        } catch (Exception e) {
            LOGGER.warn("Problem handling diagnostics message", e);
        }
    }

    public void onAdpuResponse(ReceiveSchema receiveSchema) {
        HwbCardService card = this.hwbCardService;
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

        HwbCardContext context = new HwbCardContext();
        context.setDeviceId(readerContext.getDeviceId());

        // is this a desfire card? if so then desfire native commands
        if(isDesfire(schema)) {
            context.setApduType(TransmitSchema.ApduType.DESFIRE);
            context.setHistoricalBytes(new byte[]{(byte) 0x80});
        } else {
            context.setApduType(TransmitSchema.ApduType.ISO_7816);
            context.setHistoricalBytes(new byte[]{});
        }
        // TODO set transcieve timeout?
        //context.setTranscieveTimeout();

        hwbCardService.setCardContext(context);
        hwbCardService.createTag(travelCardNumber, token, cardContent);
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
