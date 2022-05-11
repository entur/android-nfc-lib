package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaIsoDepWrapper extends AbstractReaderIsoDepWrapper {

    CommandInputOutputThread reader;

    public MinovaIsoDepWrapper(CommandInputOutputThread reader) {
        super(0);
        this.reader = reader;
    }

    @Override
    public byte[] transceive(byte[] data) throws Exception {
        //reader.transmit(data);

        return new byte[0];
    }

    @Override
    public byte[] transceiveRaw(byte[] data) throws Exception {
        return new byte[0];
    }
}
