package no.entur.android.nfc.wrapper.test.tech.transceive;

import android.os.Parcelable;

import androidx.core.util.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.wrapper.test.tech.MockBasicTagTechnologyImpl;

public class ListMockParcelableTransceive implements MockParcelableTransceive {
    protected static final byte SELECT_APPLICATION = 0x5A;

    public static Builder newBuilder() {
        return new Builder();
    }

    private interface Transceive {

    }

    private static class SimpleTransceive implements Transceive {

        private final Predicate<byte[]> command;
        private final byte[] response;
        private final boolean raw;

        public SimpleTransceive(Predicate<byte[]> command, byte[] response, boolean raw) {
            this.command = command;
            this.response = response;
            this.raw = raw;
        }
    }

    private static class ParcelableTransceive implements Transceive {

        private final Predicate<Parcelable> command;
        private final Parcelable response;

        public ParcelableTransceive(Predicate<Parcelable> command, Parcelable response) {
            this.command = command;
            this.response = response;
        }
    }

    public static class Builder {

        private List<Transceive> transceiveList = new ArrayList<>();

        private byte[] errorResponse;

        private Parcelable parcelableMetadata = null;

        public Builder withErrorResponse(byte[] errorResponse) {
            this.errorResponse = errorResponse;
            return this;
        }

        public Builder withParcelableMetadata(Parcelable parcelableMetadata) {
            this.parcelableMetadata = parcelableMetadata;
            return this;
        }


        public Builder withErrorResponse(String errorResponse) {
            this.errorResponse = MockBasicTagTechnologyImpl.hex(errorResponse);
            return this;
        }

        public Builder withTransceive(Predicate<Parcelable> command, Parcelable response) {
            transceiveList.add(new ParcelableTransceive(command, response));
            return this;
        }

        public Builder withTransceive(Predicate<byte[]> command, boolean raw, byte[] response) {
            transceiveList.add(new SimpleTransceive(command, response, raw));
            return this;
        }

        public Builder withTransceive(Predicate<byte[]> command, boolean raw, String response) {
            transceiveList.add(
                    new SimpleTransceive(
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

        public Builder withTransceiveNativeDesfireEV1SelectApplication(String applicationIdentifier, String response) {
            return withTransceiveNativeDesfireEV1SelectApplication(MockBasicTagTechnologyImpl.hex(applicationIdentifier), MockBasicTagTechnologyImpl.hex(response));
        }

        public Builder withTransceiveNativeDesfireEV1SelectApplication(byte[] applicationIdentifier, byte[] response) {
            byte[] command = new byte[applicationIdentifier.length + 1];
            command[0] = SELECT_APPLICATION;
            System.arraycopy(applicationIdentifier, 0, command, 1, applicationIdentifier.length);

            return withTransceive(command, true, response);
        }

        public Builder withTransceiveNativeDesfireEV1SelectApplication(byte[] applicationIdentifier) {
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

        public ListMockParcelableTransceive build() {
            if(errorResponse == null) {
                throw new IllegalStateException();
            }
            return new ListMockParcelableTransceive(transceiveList, errorResponse, parcelableMetadata);
        }

    }

    private final byte[] errorResponse;

    private final List<Transceive> transceiveList;
    private int index = 0;

    private Parcelable parcelableMetadata;
    public ListMockParcelableTransceive(List<Transceive> transceiveList, byte[] errorResponse, Parcelable parcelableMetadata) {
        this.transceiveList = transceiveList;
        this.errorResponse = errorResponse;
        this.parcelableMetadata = parcelableMetadata;
    }

    public byte[] transceive(byte[] data, boolean raw) throws IOException {
        if (index >= transceiveList.size()) {
            index = 0;
            return errorResponse;
        }

        Transceive transceive = transceiveList.get(index);
        index++;

        if(transceive instanceof SimpleTransceive) {
            SimpleTransceive simpleTransceive = (SimpleTransceive)transceive;
            if (simpleTransceive.raw == raw && simpleTransceive.command.test(data)) {
                return simpleTransceive.response;
            }
        }

        index = 0;
        return errorResponse;
    }

    @Override
    public <T> T parcelableTranscieve(Parcelable data) throws IOException {
        if (index >= transceiveList.size()) {
            index = 0;
            return null;
        }

        Transceive transceive = transceiveList.get(index);
        index++;

        if(transceive instanceof ParcelableTransceive) {
            ParcelableTransceive parcelableTransceive = (ParcelableTransceive)transceive;
            if (parcelableTransceive.command.test(data)) {
                return (T) parcelableTransceive.response;
            }
        }

        index = 0;
        return null;
    }

    @Override
    public Parcelable parcelableTransceiveMetadata(Parcelable data) {
        return parcelableMetadata;
    }

}