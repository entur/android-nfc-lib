package no.entur.android.nfc.external.hid.card;

import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;

public class Atr210CardAdpuSynchronizedRequestMessage extends CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> {

    public Atr210CardAdpuSynchronizedRequestMessage(NfcAdpuTransmitRequest transmit, String requestTopic, String responseTopic) {
        super(responseTopic, transmit, requestTopic);
    }

}
