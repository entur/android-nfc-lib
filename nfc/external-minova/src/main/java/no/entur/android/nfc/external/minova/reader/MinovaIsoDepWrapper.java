package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaIsoDepWrapper extends AbstractReaderIsoDepWrapper {

    CommandInputOutputThread<String, String> reader;

    public MinovaIsoDepWrapper(CommandInputOutputThread<String, String> reader, int slotNum) {
        super(slotNum);
        this.reader = reader;
    }

    @Override
    public byte[] transceive(byte[] data) throws Exception {
        String dataAsString = ByteArrayHexStringConverter.toHexString(data);
        String command = "MCR04G-8E71, CAPDU;" + dataAsString;
        String response;

        response = reader.outputInput(command);

        return ByteArrayHexStringConverter.hexStringToByteArray(response.substring(response.indexOf("=") + 1));
    }

    @Override
    public byte[] transceiveRaw(byte[] data) throws Exception {
        String dataAsString = ByteArrayHexStringConverter.toHexString(data);
        String command = "MCR04G-8E71, CAPDU;" + dataAsString;
        String response;

        response = reader.outputInput(command);

        return ByteArrayHexStringConverter.hexStringToByteArray(response.substring(response.indexOf("=") + 1));
    }
}
