package no.entur.android.nfc.external.mqtt.test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class MqttServiceExchange<T, R> {

    public <T, R> Builder newBuilder(Class<T> t, Class<R> r) {
        return new Builder<>(t, r);
    }

    private static class Builder<T, R> {

        private Class<T> t;
        private Class<R> r;

        private String readTopic;
        private String writeTopic;

        private Function<T, R> callback;

        private Consumer<Exception> errorHandler;

        public Builder(Class<T> t, Class<R> r) {
            this.t = t;
            this.r = r;
        }

        public Builder<T, R> withInputTopic(String readTopic) {
            this.readTopic = readTopic;
            return this;
        }

        public Builder<T, R> withOutputTopic(String writeTopic) {
            this.writeTopic = writeTopic;
            return this;
        }

        public Builder<T, R> withCallback(Function<T, R> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<T, R> withErrorHandler(Consumer<Exception> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public MqttServiceExchange<T, R> build() {
            return new MqttServiceExchange<>(readTopic, writeTopic, t, r, callback, errorHandler);
        }

    }

    public MqttServiceExchange(String readTopic, String writeTopic, Class<T> t, Class<R> r, Function<T, R> callback, Consumer<Exception> errorHandler) {
        this.readTopic = readTopic;
        this.writeTopic = writeTopic;
        this.t = t;
        this.r = r;
        this.callback = callback;
        this.errorHandler = errorHandler;
    }

    protected ObjectMapper objectMapper = new ObjectMapper();

    private String readTopic;
    private String writeTopic;

    private Class<T> t;
    private Class<R> r;

    private Function<T, R> callback;

    private Consumer<Exception> errorHandler;

    public String getReadTopic() {
        return readTopic;
    }

    public String getWriteTopic() {
        return writeTopic;
    }

    public Class<T> getT() {
        return t;
    }

    public Class<R> getR() {
        return r;
    }

    public Function<T, R> getCallback() {
        return callback;
    }

    public Consumer<Exception> getErrorHandler() {
        return errorHandler;
    }

    public byte[] apply(byte[] inputBytes) throws IOException {
        T in = objectMapper.readValue(inputBytes, t);
        R out = callback.apply(in);

        // output bytes
        return objectMapper.writeValueAsBytes(out);
    }

}
