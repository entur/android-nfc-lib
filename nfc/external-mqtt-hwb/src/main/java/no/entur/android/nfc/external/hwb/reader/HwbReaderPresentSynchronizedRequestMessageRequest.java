package no.entur.android.nfc.external.hwb.reader;

import java.util.UUID;

import hwb.utilities.device.diagnostics.request.RequestSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentSynchronizedRequestMessageRequest;

public class HwbReaderPresentSynchronizedRequestMessageRequest extends ReaderPresentSynchronizedRequestMessageRequest<String, RequestSchema>  {

    public HwbReaderPresentSynchronizedRequestMessageRequest(RequestSchema requestSchema, String deviceId) {
        super(deviceId, requestSchema, "/device/" + deviceId + "/diagnostics");
    }
}
