package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaIsoDepWrapper extends AbstractReaderIsoDepWrapper {

    private final MinovaCommands commands;

    public MinovaIsoDepWrapper(CommandInputOutputThread<String, String> reader) {
        super(-1);
        this.commands = new MinovaCommands(reader);
    }

    @Override
    public byte[] transceive(byte[] data) throws Exception {
        return commands.sendAdpu(data);
    }

    @Override
    public byte[] transceiveRaw(byte[] data) throws Exception {
        return commands.sendAdpu(data);
    }
}
