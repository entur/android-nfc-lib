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
import no.entur.android.nfc.external.hwb.HwbMqttClient;
import no.entur.android.nfc.external.hwb.HwbService;
import no.entur.android.nfc.external.hwb.card.HwbCardService;
import no.entur.android.nfc.external.hwb.card.HwbCardContext;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HwbReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbReaderService.class);

    private HwbReaderContext readerContext;

    private HwbReaderCommands readerCommands;

    private HwbCardService hwbCardService;

    private Context context;

    private SynchronizedRequestResponseMessages<String> diagnosticsRequestResponseMessages = new SynchronizedRequestResponseMessages<>();

    private final HwbMqttClient hwbMqttClient;

    public HwbReaderService(Context context, HwbMqttClient hwbMqttClient, SynchronizedRequestResponseMessages<UUID> adpuRequestResponseMessages, HwbReaderContext readerContext, long transcieveTimeout) {
        this.context = context;
        this.hwbMqttClient = hwbMqttClient;
        this.readerContext = readerContext;

        this.hwbCardService = new HwbCardService(adpuRequestResponseMessages, hwbMqttClient, transcieveTimeout);

        this.readerCommands = HwbReaderCommands.newBuilder()
                .withReaderContext(readerContext)
                .withReaderMessageConverter(new HwbReaderMessageConverter())
                .withSynchronizedRequestResponseMessages(diagnosticsRequestResponseMessages)
                .build();
    }

    public void open() {
        hwbMqttClient.subscribe("/device/" + readerContext.getDeviceId() + "/diagnostics", DiagnosticsSchema.class, this::diagnostics);

        broadcastOpened();
    }

    public void close() {
        try {
            hwbMqttClient.unsubscribe("/device/" + readerContext.getDeviceId() + "/diagnostics");
        } finally {
            broadcastClosed();
        }
    }

    public void broadcastClosed() {
        broadcast(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
    }

    public void broadcastOpened() {
        // TODO add reader controls
        broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);
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

        // is this a desfire card? if so then desfire native commands
        if(isDesfire(schema)) {
            context.setApduType(TransmitSchema.ApduType.DESFIRE);
        } else {
            context.setApduType(TransmitSchema.ApduType.ISO_7816);
        }

        hwbCardService.setCardContext(context);


        // broadcast tag present
        // add one or more metadata fields depending on what is included
        // block any previuos tag from talking to the new one
        hwbCardService.createTag(travelCardNumber, token, cardContent);

        // TODO
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

    public void broadcast(String action) {
        LOGGER.info("Broadcast " + action);
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent, HwbService.ANDROID_PERMISSION_NFC);
    }

    public boolean isPresent(long timeout) throws IOException {
        return readerCommands.isPresent(timeout);
    }
}
