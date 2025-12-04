package no.entur.android.nfc.external.hwb.intent.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.hwb.HwbMqttService;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class HwbServiceCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbServiceCommandsWrapper.class);

    private final HwbMqttService hwbService;

    public HwbServiceCommandsWrapper(HwbMqttService hwbService) {
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

}
