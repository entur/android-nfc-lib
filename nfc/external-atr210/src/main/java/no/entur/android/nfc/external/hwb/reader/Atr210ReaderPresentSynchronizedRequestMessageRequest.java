package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.device.diagnostics.request.RequestSchema;
import no.entur.android.nfc.mqtt.messages.reader.ReaderPresentSynchronizedRequestMessageRequest;

public class Atr210ReaderPresentSynchronizedRequestMessageRequest extends ReaderPresentSynchronizedRequestMessageRequest<String, RequestSchema>  {

    public Atr210ReaderPresentSynchronizedRequestMessageRequest(RequestSchema requestSchema, String deviceId) {
        super(deviceId, requestSchema, "/device/" + deviceId + "/diagnostics");
    }
}
