package no.entur.android.nfc.external.hwb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.entur.android.nfc.external.hwb.reader.HwbReaderPresentRequestMessage;
import no.entur.android.nfc.mqtt.messages.RequestMessage;
import no.entur.android.nfc.mqtt.messages.RequestMessageListener;
import no.entur.android.nfc.mqtt.messages.ResponseMessageListener;

public class HwbMessageListener implements RequestMessageListener<String> {

    protected Map<String, List<ResponseMessageListener<String>>> listeners = new ConcurrentHashMap<>();

    @Override
    public void onRequestMessage(RequestMessage<String> requestMessage, ResponseMessageListener<String> listener) {

        if(requestMessage instanceof HwbReaderPresentRequestMessage) {
            HwbReaderPresentRequestMessage m = (HwbReaderPresentRequestMessage)requestMessage;

            String outgoing = "/device/" + m.getDeviceId() + "/diagnostics/request";


            String incoming = "/device/" + m.getDeviceId() + "/diagnostics";

        }

    }
}
