package no.entur.android.nfc.external.hwb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.entur.android.nfc.external.hwb.reader.HwbReaderPresentSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedRequestMessageListener;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessageListener;

public class HwbSynchronizedRequestMessageListener implements SynchronizedRequestMessageListener<String> {

    protected Map<String, List<SynchronizedResponseMessageListener<String>>> listeners = new ConcurrentHashMap<>();

    @Override
    public void onRequestMessage(SynchronizedRequestMessageRequest<String> synchronizedRequestMessage, SynchronizedResponseMessageListener<String> listener) {

        if(synchronizedRequestMessage instanceof HwbReaderPresentSynchronizedRequestMessageRequest) {
            HwbReaderPresentSynchronizedRequestMessageRequest m = (HwbReaderPresentSynchronizedRequestMessageRequest) synchronizedRequestMessage;

            String outgoing = "/device/" + m.getDeviceId() + "/diagnostics/request";


            String incoming = "/device/" + m.getDeviceId() + "/diagnostics";

        }

    }
}
