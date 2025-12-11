package no.entur.android.nfc.external.hwb.intent.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.hwb.Atr210MqttService;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class Atr210ServiceCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ServiceCommandsWrapper.class);

    private final Atr210MqttService hwbService;

    public Atr210ServiceCommandsWrapper(Atr210MqttService hwbService) {
        this.hwbService = hwbService;
    }

    public byte[] discoverReaders() {
        Exception exception = null;
        try {
            hwbService.discoverReaders();
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
            result = new ArrayList<>(hwbService.getReaderIds());
        } catch (Exception e) {
            LOGGER.debug("Problem discovering readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }


}
