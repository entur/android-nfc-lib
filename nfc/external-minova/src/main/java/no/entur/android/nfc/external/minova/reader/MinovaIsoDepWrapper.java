package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.service.tag.TagProxy;
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
        try {
            return commands.sendAdpu(data);
        } catch(McrReaderException e) {
            // there is no "tag lost" even, so make sure to loose the tag as soon as possible if some command does not respond
            tagProxy.close();

            throw e;
        }
    }

    @Override
    public byte[] transceiveRaw(byte[] data) throws Exception {
        try {
            return commands.sendAdpu(data);
        } catch(McrReaderException e) {
            // there is no "tag lost" even, so make sure to loose the tag as soon as possible if some command does not respond
            tagProxy.close();

            throw e;
        }
    }
}
