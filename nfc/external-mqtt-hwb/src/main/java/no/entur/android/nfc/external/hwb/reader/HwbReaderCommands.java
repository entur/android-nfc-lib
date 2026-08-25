package no.entur.android.nfc.external.hwb.reader;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import hwb.utilities.validators.deviceId.response.ResponseSchema;
import hwb.utilities.validators.deviceId.response.Result;
import no.entur.android.nfc.external.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderCommands;

public class HwbReaderCommands extends ReaderCommands<String, HwbReaderContext>  {

    // subscribes to topics
    // /device/[deviceId]/diagnostics <- private topic
    // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private SynchronizedRequestResponseMessages synchronizedRequestResponseMessages;

        private HwbReaderMessageConverter readerMessageConverter;

        private HwbReaderContext readerContext;
        private MqttServiceClient mqttClient;

        public Builder withSynchronizedRequestResponseMessages(SynchronizedRequestResponseMessages synchronizedRequestResponseMessages) {
            this.synchronizedRequestResponseMessages = synchronizedRequestResponseMessages;
            return this;
        }

        public Builder withReaderMessageConverter(HwbReaderMessageConverter readerMessageConverter) {
            this.readerMessageConverter = readerMessageConverter;
            return this;
        }

        public Builder withReaderContext(HwbReaderContext readerContext) {
            this.readerContext = readerContext;
            return this;
        }

        public Builder withMqttClient(MqttServiceClient mqttClient) {
            this.mqttClient = mqttClient;
            return this;
        }

        public HwbReaderCommands build() {
            return new HwbReaderCommands(readerContext, synchronizedRequestResponseMessages, readerMessageConverter, mqttClient);
        }
    }

    protected HwbReaderMessageConverter converter;
    protected final MqttServiceClient mqttClient;

    public HwbReaderCommands(HwbReaderContext readerContext, SynchronizedRequestResponseMessages readerExchange, HwbReaderMessageConverter converter, MqttServiceClient mqttClient) {
        super(readerContext, readerExchange);

        this.converter = converter;
        this.mqttClient = mqttClient;
    }

    public DiagnosticsSchema getDiagnostics(long readerPresentTimeout) throws IOException {
        ReaderPresentSynchronizedRequestMessageRequest<String, ?> request = converter.createReaderPresentRequestMessage(readerContext);

        SynchronizedResponseMessage<String> response = readerExchange.sendAndWaitForResponse(request, readerPresentTimeout);

        if (response != null) {
            HwbReaderPresentSynchronizedResponseMessage result = converter.createReaderPresentResponseMessage(response, readerContext);
            return result.getPayload();
        }
        throw new IOException();
    }

    public Boolean isPresent(long duration) {
        try {
            return getDiagnostics(duration) != null;
        } catch(Exception e) {
            return false;
        }
    }

    public void setControlResult(String validity, String title, String description) throws IOException {
        ResponseSchema schema = new ResponseSchema();
        schema.setEventTimestamp(new Date());
        schema.setTraceId(UUID.randomUUID());

        Result result = new Result();

        result.setValidity(validity);
        result.setTitle(title);
        result.setDescription(description);

        schema.setResult(result);

        // validators/[deviceId]/response

        String requestTopic = "validators/" + readerContext.getDeviceId() + "/response";

        mqttClient.publishAsJson(requestTopic, schema);
    }


}
