package no.entur.android.nfc.external.hwb.reader;

import java.io.IOException;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderCommands;

public class Atr210ReaderCommands extends ReaderCommands<String, Atr210ReaderContext>  {

    // subscribes to topics
    // /device/[deviceId]/diagnostics <- private topic
    // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private SynchronizedRequestResponseMessages synchronizedRequestResponseMessages;

        private Atr210ReaderMessageConverter readerMessageConverter;

        private Atr210ReaderContext readerContext;

        public Builder withSynchronizedRequestResponseMessages(SynchronizedRequestResponseMessages synchronizedRequestResponseMessages) {
            this.synchronizedRequestResponseMessages = synchronizedRequestResponseMessages;
            return this;
        }

        public Builder withReaderMessageConverter(Atr210ReaderMessageConverter readerMessageConverter) {
            this.readerMessageConverter = readerMessageConverter;
            return this;
        }

        public Builder withReaderContext(Atr210ReaderContext readerContext) {
            this.readerContext = readerContext;
            return this;
        }

        public Atr210ReaderCommands build() {
            return new Atr210ReaderCommands(readerContext, synchronizedRequestResponseMessages, readerMessageConverter);
        }
    }

    private Atr210ReaderMessageConverter converter;

    public Atr210ReaderCommands(Atr210ReaderContext readerContext, SynchronizedRequestResponseMessages readerExchange, Atr210ReaderMessageConverter converter) {
        super(readerContext, readerExchange, converter);

        this.converter = converter;
    }

    public DiagnosticsSchema getDiagnostics(long readerPresentTimeout) throws IOException {
        ReaderPresentSynchronizedRequestMessageRequest<String, ?> request = readerPresentMessageConverter.createReaderPresentRequestMessage(readerContext);

        SynchronizedResponseMessage<String> response = readerExchange.sendAndWaitForResponse(request, readerPresentTimeout);

        if (response != null) {
            Atr210ReaderPresentSynchronizedResponseMessage result = converter.createReaderPresentResponseMessage(response, readerContext);
            return result.getPayload();
        }
        throw new IOException();
    }

}
