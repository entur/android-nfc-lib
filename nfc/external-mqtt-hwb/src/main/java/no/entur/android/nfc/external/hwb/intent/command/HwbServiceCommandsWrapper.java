package no.entur.android.nfc.external.hwb.intent.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.hwb.HwbMqttHandler;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class HwbServiceCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbServiceCommandsWrapper.class);

    private final HwbMqttHandler hwbMqttHandler;

    public HwbServiceCommandsWrapper(HwbMqttHandler hwbMqttHandler) {
        this.hwbMqttHandler = hwbMqttHandler;
    }

    public byte[] discoverReaders() {
        Exception exception = null;
        try {
            hwbMqttHandler.discoverReaders();
        } catch (Exception e) {
            LOGGER.debug("Problem discovering readers", e);

            exception = e;
        }
        return returnValue(exception);
    }

    public byte[] getReaderIds() {
        Exception exception = null;
        List<String> result = null;
        try {
            result = new ArrayList<>(hwbMqttHandler.getReaderIds());
        } catch (Exception e) {
            LOGGER.debug("Problem discovering readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }


}
