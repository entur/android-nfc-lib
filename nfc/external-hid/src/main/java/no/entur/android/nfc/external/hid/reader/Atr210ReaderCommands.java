package no.entur.android.nfc.external.hid.reader;

import java.io.IOException;

import hwb.utilities.mqtt3.client.MqttServiceClient;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;
import no.entur.android.nfc.external.hid.dto.atr210.TicketResponse;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderCommands;

public class Atr210ReaderCommands extends ReaderCommands<String, Atr210ReaderContext>  {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private SynchronizedRequestResponseMessages synchronizedRequestResponseMessages;

        private Atr210ReaderContext readerContext;
        private MqttServiceClient mqttClient;

        public Builder withSynchronizedRequestResponseMessages(SynchronizedRequestResponseMessages synchronizedRequestResponseMessages) {
            this.synchronizedRequestResponseMessages = synchronizedRequestResponseMessages;
            return this;
        }

        public Builder withReaderContext(Atr210ReaderContext readerContext) {
            this.readerContext = readerContext;
            return this;
        }

        public Builder withMqttClient(MqttServiceClient mqttClient) {
            this.mqttClient = mqttClient;
            return this;
        }

        public Atr210ReaderCommands build() {
            return new Atr210ReaderCommands(readerContext, synchronizedRequestResponseMessages, mqttClient);
        }

    }

    protected final MqttServiceClient mqttClient;

    public Atr210ReaderCommands(Atr210ReaderContext readerContext, SynchronizedRequestResponseMessages readerExchange, MqttServiceClient mqttClient) {
        super(readerContext, readerExchange);
        this.mqttClient = mqttClient;
    }

    public NfcConfiguationResponse setNfcReadersConfiguration(NfcConfiguationRequest request, long timeout) throws IOException {
        String requestTopic = "itxpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/configuration/request";
        String responseTopic = "itxpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/configuration";

        Atr210FirstMessageOnTopicSynchronizedRequestMessage r = new Atr210FirstMessageOnTopicSynchronizedRequestMessage(request, requestTopic, responseTopic);

        SynchronizedResponseMessage<String> response = readerExchange.sendAndWaitForResponse(r, timeout);

        if (response != null) {
            Atr210FirstMessageOnTopicSynchronizedResponseMessage result = (Atr210FirstMessageOnTopicSynchronizedResponseMessage)response;
            return (NfcConfiguationResponse) result.getPayload();
        }
        throw new IOException();
    }

    public ReadersStatusResponse getNfcReaders(long timeout) throws IOException {
        String requestTopic = "itxpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers/request";
        String responseTopic = "itxpt/ticketreader/" + readerContext.getProviderId() + "/nfc/readers";

        Atr210FirstMessageOnTopicSynchronizedRequestMessage r = new Atr210FirstMessageOnTopicSynchronizedRequestMessage(null, requestTopic, responseTopic);

        SynchronizedResponseMessage<String> response = readerExchange.sendAndWaitForResponse(r, timeout);

        if (response != null) {
            Atr210FirstMessageOnTopicSynchronizedResponseMessage result = (Atr210FirstMessageOnTopicSynchronizedResponseMessage)response;
            return (ReadersStatusResponse) result.getPayload();
        }
        throw new IOException();
    }

    public NfcConfiguationResponse getNfcReadersConfiguration(long timeout) throws IOException {
        // use empty payload to get configuration
        return setNfcReadersConfiguration(new NfcConfiguationRequest(), timeout);
    }

    /**
     *
     * Get id
     *
     * @return id
     * @see no.entur.android.nfc.external.hid.intent.Atr210Reader
     */

    public String getId() {
        return readerContext.getClientId() + "-" + readerContext.getProviderId();
    }

    public void setResult(boolean valid, String led, String sound) throws IOException {
        String requestTopic = "itxpt/ticketreader/" + readerContext.getProviderId() + "/response/validation";

        TicketResponse response = new TicketResponse();
        response.setLed(led);
        response.setValid(valid);
        response.setSound(sound);

        mqttClient.publishAsJson(requestTopic, response);
    }



}
