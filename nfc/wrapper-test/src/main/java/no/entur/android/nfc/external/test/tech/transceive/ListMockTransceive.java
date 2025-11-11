package no.entur.android.nfc.external.test.tech.transceive;

import androidx.core.util.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.test.tech.MockBasicTagTechnologyImpl;

public class ListMockTransceive implements MockTransceive {

    public static Builder newBuilder() {
        return new Builder();
    }

    private static class Transceive {

        private final Predicate<byte[]> command;
        private final byte[] response;

        public Transceive(Predicate<byte[]> command, byte[] response) {
            this.command = command;
            this.response = response;
        }
    }

    public static class Builder {

        private List<Transceive> transceiveList = new ArrayList<>();

        private byte[] errorResponse;

        public Builder withErrorResponse(byte[] errorResponse) {
            this.errorResponse = errorResponse;
            return this;
        }

        public Builder withErrorResponse(String errorResponse) {
            this.errorResponse = MockBasicTagTechnologyImpl.hex(errorResponse);
            return this;
        }

        public Builder withTransceive(Predicate<byte[]> command, byte[] response) {
            transceiveList.add(new Transceive(command, response));
            return this;
        }

        public Builder withTransceive(Predicate<byte[]> command, String response) {
            transceiveList.add(
                    new Transceive(
                        command,
                        MockBasicTagTechnologyImpl.hex(response)
                    )
            );
            return this;
        }

        public Builder withTransceive(String command, String response) {
            return withTransceive(MockBasicTagTechnologyImpl.hex(command), MockBasicTagTechnologyImpl.hex(response));
        }

        public Builder withTransceive(byte[] command, byte[] response) {
            withTransceive(new ByteArrayPredicate(command), response);
            return this;
        }

        public ListMockTransceive build() {
            if(errorResponse == null) {
                throw new IllegalStateException();
            }
            return new ListMockTransceive(transceiveList, errorResponse);
        }

    }

    private final byte[] errorResponse;

    private final List<Transceive> transceiveList;
    private int index = 0;

    public ListMockTransceive(List<Transceive> transceiveList, byte[] errorResponse) {
        this.transceiveList = transceiveList;
        this.errorResponse = errorResponse;
    }

    public byte[] transceive(byte[] data) throws IOException {
        if (index >= transceiveList.size()) {
            index = 0;
            return errorResponse;
        }

        Transceive transceive = transceiveList.get(index);
        index++;

        if(transceive.command.test(data)) {
            return transceive.response;
        }

        index = 0;
        return errorResponse;
    }

}