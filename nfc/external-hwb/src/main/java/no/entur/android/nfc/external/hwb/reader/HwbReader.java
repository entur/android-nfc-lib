package no.entur.android.nfc.external.hwb.reader;

import android.content.Context;
import android.content.Intent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.validators.nfc.CardContent;
import hwb.utilities.validators.nfc.NfcSchema;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.hwb.HwbService;
import no.entur.android.nfc.external.hwb.card.HwbCard;
import no.entur.android.nfc.external.hwb.card.HwbCardContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HwbReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbReader.class);

    private long timeout = 1000;

    private Mqtt3AsyncClient client;

    private Executor executor;

    private HwbReaderContext readerContext;

    private ObjectMapper objectMapper;

    private HwbCard hwbCard;

    private Context context;

    public HwbReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean connect() throws Exception {
        CompletableFuture<@NotNull Mqtt3ConnAck> connect = client.connect();

        Mqtt3ConnAck ack = connect.get(timeout, TimeUnit.MILLISECONDS);

        if(ack.getReturnCode() == Mqtt3ConnAckReturnCode.SUCCESS) {

            // TODO send a diagnostics message here and wait for response?

            broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);

            return true;
        }
        return false;
    }

    public void subscribe() {
        // subscribes to topics
        // /device/[deviceId]/diagnostics <- private topic
        // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

        client.subscribeWith()
                .topicFilter("/device/" + readerContext.getDeviceId() + "/diagnostics")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(this::diagnostics)
                .executor(executor)
                .send();

        client.subscribeWith()
                .topicFilter("/validators/nfc")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(this::diagnostics)
                .executor(executor)
                .send();


    }

    public void card(Mqtt3Publish publish) {
        byte[] payloadAsBytes = publish.getPayloadAsBytes();
        try {
            ReceiveSchema receiveSchema = objectMapper.readValue(payloadAsBytes, ReceiveSchema.class);


        } catch (Exception e) {

        }


    }

    public void diagnostics(Mqtt3Publish publish) {
        byte[] payloadAsBytes = publish.getPayloadAsBytes();

        try {
            DiagnosticsSchema diagnosticsSchema = objectMapper.readValue(payloadAsBytes, DiagnosticsSchema.class);


        } catch (Exception e) {

        }

    }


    public void unsubscribe() {
        client.unsubscribeWith()
                .topicFilter("/device/" + readerContext.getDeviceId() + "/diagnostics")
                .send();
    }

    public void disconnect() {
        client.disconnect();

        broadcast(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
    }


    public void connectReader() {
        broadcast(ExternalNfcReaderCallback.ACTION_READER_OPENED);
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

        hwbCard.setContext(context);

        hwbCard.createTag(travelCardNumber, token, cardContent);

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
}
