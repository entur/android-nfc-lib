package no.entur.android.nfc.external.test.tech.transceive;

import androidx.core.util.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.test.tech.MockBasicTagTechnologyImpl;

public class ListMockTransceive implements MockTransceive {
    protected static final byte SELECT_APPLICATION = 0x5A;

    public static Builder newBuilder() {
        return new Builder();
    }

    private static class Transceive {

        private final Predicate<byte[]> command;
        private final byte[] response;
        private final boolean raw;

        public Transceive(Predicate<byte[]> command, byte[] response, boolean raw) {
            this.command = command;
            this.response = response;
            this.raw = raw;
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

        public Builder withTransceive(Predicate<byte[]> command, boolean raw, byte[] response) {
            transceiveList.add(new Transceive(command, response, raw));
            return this;
        }

        public Builder withTransceive(Predicate<byte[]> command, boolean raw, String response) {
            transceiveList.add(
                    new Transceive(
                        command,
                        MockBasicTagTechnologyImpl.hex(response),
                            raw
                    )
            );
            return this;
        }

        public Builder withTransceive(String command, boolean raw, String response) {
            return withTransceive(MockBasicTagTechnologyImpl.hex(command), raw, MockBasicTagTechnologyImpl.hex(response));
        }

        public Builder withTransceiveNativeDesfireSelectApplication(String applicationIdentifier, String response) {
            return withTransceiveNativeDesfireSelectApplication(MockBasicTagTechnologyImpl.hex(applicationIdentifier), MockBasicTagTechnologyImpl.hex(response));
        }

        public Builder withTransceiveNativeDesfireSelectApplication(byte[] applicationIdentifier, byte[] response) {
            byte[] command = new byte[applicationIdentifier.length + 1];
            command[0] = SELECT_APPLICATION;
            System.arraycopy(applicationIdentifier, 0, command, 1, applicationIdentifier.length);

            return withTransceive(command, true, response);
        }

        public Builder withTransceiveNativeDesfireSelectApplication(byte[] applicationIdentifier) {
            byte[] command = new byte[applicationIdentifier.length + 1];
            command[0] = SELECT_APPLICATION;
            System.arraycopy(applicationIdentifier, 0, command, 1, applicationIdentifier.length);

            byte[] okResponse = new byte[]{0x00};

            return withTransceive(command, true, okResponse);
        }

        public Builder withTransceiveSelectApplication(String applicationIdentifier, String response) {
            return withTransceiveSelectApplication(MockBasicTagTechnologyImpl.hex(applicationIdentifier), true, MockBasicTagTechnologyImpl.hex(response));
        }

        public Builder withTransceiveSelectApplication(byte[] applicationIdentifier, boolean raw, byte[] response) {
            byte[] command = buildSelectApplicationCommand(applicationIdentifier);

            return withTransceive(command, raw, response);
        }

        private byte[] buildSelectApplicationCommand(byte[] bytes) {
            byte[] command = new byte[6 + bytes.length];
            command[0] = (byte) 0x00; // CLA
            command[1] = (byte) 0xA4; // INS
            command[2] = (byte) 0x04; // P1
            // 3: 0x00 P2
            // 4: payload length
            // 5...n-1 : application id
            // n: Lc
            command[4] = (byte) bytes.length;

            System.arraycopy(bytes, 0, command, 5, bytes.length);

            return command;
        }


        public Builder withTransceive(byte[] command, boolean raw, byte[] response) {
            withTransceive(new ByteArrayPredicate(command), raw, response);
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

    public byte[] transceive(byte[] data, boolean raw) throws IOException {
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