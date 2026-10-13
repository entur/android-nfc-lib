package no.entur.android.nfc.external.hwb.reader;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.validators.barcode.BarcodeSchema;
import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalBarcodeCallback;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.hwb.HwbMqttHandler;
import no.entur.android.nfc.external.hwb.card.HwbCardService;
import no.entur.android.nfc.external.hwb.card.HwbCardContext;
import no.entur.android.nfc.external.hwb.intent.HwbReader;
import no.entur.android.nfc.external.hwb.intent.bind.HwbReaderBinder;
import no.entur.android.nfc.external.hwb.intent.bind.HwbReaderTechnology;
import no.entur.android.nfc.external.hwb.intent.command.DefaultHwbReaderCommandsWrapper;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.service.tag.INFcTagBinder;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HwbReaderService implements SynchronizedRequestMessageListener<String> {

    private static final long HEARTBEAT_LEEWAY = 3000;

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbReaderService.class);

    private final INFcTagBinder infcTagBinder;

    protected HwbReaderContext readerContext;

    protected HwbReaderCommands readerCommands;

    protected HwbCardService hwbCardService;

    protected Context context;

    protected SynchronizedRequestResponseMessages<String> requestResponseMessages = new SynchronizedRequestResponseMessages<>();

    protected final MqttServiceClient mqttServiceClient;

    protected HwbReader hwbReader;

    protected final TagProxyStore tagProxyStore;

    protected long nextHeartbeatDeadline = -1L;

    public HwbReaderService(Context context, MqttServiceClient mqttServiceClient, SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, HwbReaderContext readerContext, long transcieveTimeout, TagProxyStore tagProxyStore) {
        this.context = context;
        this.mqttServiceClient = mqttServiceClient;
        this.readerContext = readerContext;

        this.readerCommands = HwbReaderCommands.newBuilder()
                .withReaderContext(readerContext)
                .withReaderMessageConverter(new HwbReaderMessageConverter())
                .withSynchronizedRequestResponseMessages(requestResponseMessages)
                .withMqttClient(mqttServiceClient)
                .build();

        this.tagProxyStore = tagProxyStore;

        HwbReaderBinder binder = new HwbReaderBinder();
        binder.setReaderCommandsWrapper(new DefaultHwbReaderCommandsWrapper(readerCommands));
        this.hwbReader = new HwbReader("HWB", binder);

        infcTagBinder = new INFcTagBinder(tagProxyStore);
        infcTagBinder.setReaderTechnology(new HwbReaderTechnology(true));

        this.hwbCardService = new HwbCardService(context, adpuRequestResponseMessages, mqttServiceClient, transcieveTimeout, infcTagBinder, tagProxyStore);

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

    private void subscribe() {
        mqttServiceClient.subscribeToJson("/device/" + readerContext.getDeviceId() + "/diagnostics", this::diagnostics, DiagnosticsSchema.class);
    }

    private void unsubscribe() {
        mqttServiceClient.unsubscribe("/device/" + readerContext.getDeviceId() + "/diagnostics");
    }

    public void broadcastClosed() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

        if(hwbReader != null) {
            intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, hwbReader.getId());
        }

        context.sendBroadcast(intent, HwbMqttHandler.ANDROID_PERMISSION_NFC);
    }

    public void broadcastOpened() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);

        intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, hwbReader);

        context.sendBroadcast(intent, HwbMqttHandler.ANDROID_PERMISSION_NFC);
    }

    protected void diagnostics(DiagnosticsSchema diagnosticsSchema) {
        try {
            requestResponseMessages.onResponseMessage(new HwbReaderPresentSynchronizedResponseMessage(diagnosticsSchema, true));
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

        context.setTraceId(schema.getTraceId());
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

    public boolean hasHeartbeat() {
        return nextHeartbeatDeadline + HEARTBEAT_LEEWAY >= System.currentTimeMillis();
    }

    public void setNextHeartbeatDeadline(long l) {
        this.nextHeartbeatDeadline = l;
    }

    public void setDiagnosticsSchema(hwb.utilities.device.diagnostics.DiagnosticsSchema heartbeat) {
        this.readerContext.setDiagnosticsSchema(heartbeat);
    }

    public void barcode(BarcodeSchema barcodeSchema) {
        try {
            Intent intent = new Intent(ExternalBarcodeCallback.ACTION_BARCODE_DISCOVERED);

            String barcode = barcodeSchema.getBarcode();

            byte[] bytes = Base64.decode(barcode, Base64.NO_WRAP);

            intent.putExtra(ExternalBarcodeCallback.BARCODE_EXTRA_BODY, bytes);
            intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, hwbReader.getId());

            context.sendBroadcast(intent, HwbMqttHandler.ANDROID_PERMISSION_NFC);
        } catch (Exception e) {
            LOGGER.warn("Problem handling barcode message", e);
        }
    }

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<String> message, SynchronizedResponseMessageListener<String> listener) throws IOException {
        mqttServiceClient.publishAsJson(message.getTopic(), message.getPayload());
    }

}
