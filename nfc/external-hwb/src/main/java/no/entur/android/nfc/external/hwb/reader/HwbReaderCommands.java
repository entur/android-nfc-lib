package no.entur.android.nfc.external.hwb.reader;

import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestResponseMessages;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;
import no.entur.android.nfc.mqtt.messages.reader.ReaderCommands;

public class HwbReaderCommands extends ReaderCommands<String, HwbReaderContext> implements SynchronizedRequestMessageListener<String> {

    // subscribes to topics
    // /device/[deviceId]/diagnostics <- private topic
    // /validators/nfc/apdu/receive <- shared topic, used here only for ADPU exchange

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private SynchronizedRequestResponseMessages synchronizedRequestResponseMessages;

        private long presentTimeout;

        private HwbReaderMessageConverter readerMessageConverter;

        private HwbReaderContext readerContext;

        public Builder withSynchronizedRequestResponseMessages(SynchronizedRequestResponseMessages synchronizedRequestResponseMessages) {
            this.synchronizedRequestResponseMessages = synchronizedRequestResponseMessages;
            return this;
        }

        public Builder withPresentTimeout(long presentTimeout) {
            this.presentTimeout = presentTimeout;
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
            return new HwbReaderCommands(readerContext, synchronizedRequestResponseMessages, presentTimeout, readerMessageConverter);
        }
    }

    public HwbReaderCommands(HwbReaderContext readerContext, SynchronizedRequestResponseMessages readerExchange, long presentTimeout, HwbReaderMessageConverter converter) {
        super(readerContext, readerExchange, presentTimeout, converter);
    }

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<String> message, SynchronizedResponseMessageListener<String> listener) {

    }

}
