package no.entur.android.nfc.external.atr210.intent.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.atr210.Atr210MqttHandler;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class Atr210ServiceCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ServiceCommandsWrapper.class);

    private final Atr210MqttHandler atr210MqttService;

    public Atr210ServiceCommandsWrapper(Atr210MqttHandler atr210MqttService) {
        this.atr210MqttService = atr210MqttService;
    }

    public byte[] getReaderIds() {
        Exception exception = null;
        List<String> result = null;
        try {
            result = new ArrayList<>(atr210MqttService.getReaderIds());
        } catch (Exception e) {
            LOGGER.debug("Problem discovering readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }


}
