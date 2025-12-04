package no.entur.android.nfc.external.hwb.reader;

import java.io.IOException;

import hwb.utilities.device.deviceId.diagnostics.DiagnosticsSchema;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentResponseMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;
import no.entur.android.nfc.mqtt.messages.reader.ReaderCommands;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageTimeoutException;

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

        public HwbReaderCommands build() {
            return new HwbReaderCommands(readerContext, synchronizedRequestResponseMessages, readerMessageConverter);
        }
    }

    private HwbReaderMessageConverter converter;

    public HwbReaderCommands(HwbReaderContext readerContext, SynchronizedRequestResponseMessages readerExchange, HwbReaderMessageConverter converter) {
        super(readerContext, readerExchange, converter);

        this.converter = converter;
    }

    public DiagnosticsSchema getDiagnostics(long readerPresentTimeout) throws IOException {
        ReaderPresentSynchronizedRequestMessageRequest<String, ?> request = readerPresentMessageConverter.createReaderPresentRequestMessage(readerContext);

        SynchronizedResponseMessage<String> response = readerExchange.sendAndWaitForResponse(request, readerPresentTimeout);

        if (response != null) {
            HwbReaderPresentSynchronizedResponseMessage result = converter.createReaderPresentResponseMessage(response, readerContext);
            return result.getPayload();
        }
        throw new IOException();
    }

}
