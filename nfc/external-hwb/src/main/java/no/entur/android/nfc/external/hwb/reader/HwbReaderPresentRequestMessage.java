package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.diagnostics.request.RequestSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentRequestMessage;

public class HwbReaderPresentRequestMessage extends ReaderPresentRequestMessage<String> implements DeviceHwbMessage {

    private final RequestSchema requestSchema;
    private final String deviceId;

    public HwbReaderPresentRequestMessage(RequestSchema requestSchema, String deviceId) {
        super(requestSchema.getTraceId().toString());
        this.requestSchema = requestSchema;
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }
}
