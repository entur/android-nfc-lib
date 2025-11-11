package no.entur.android.nfc.external.test.tech.transceive;

import java.io.IOException;

import no.entur.android.nfc.external.test.tech.MockBasicTagTechnologyImpl;

public class DefaultMockTransceive implements MockTransceive {

    private final byte[] command;
    private final byte[] response;

    public DefaultMockTransceive(byte[] command, byte[] response) {
        this.command = command;
        this.response = response;
    }

    public byte[] getCommand() {
        return command;
    }

    public byte[] getResponse() {
        return response;
    }

    @Override
    public byte[] transceive(byte[] data) throws IOException {
        if(isCommand(data)) {
            return response;
        }
        throw new IOException("Mock transceive command " + MockBasicTagTechnologyImpl.toHexString(data) + " does not match expected " + MockBasicTagTechnologyImpl.toHexString(command));
    }

    private boolean isCommand(byte[] data) throws IOException {
        if(data.length != command.length) {
            return false;
        }
        for(int i = 0; i < command.length; i++) {
            if(command[i] != data[i]) {
                return false;
            }
        }
        return true;
    }
}
