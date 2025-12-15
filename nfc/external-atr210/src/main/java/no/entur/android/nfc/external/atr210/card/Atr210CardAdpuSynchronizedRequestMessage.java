package no.entur.android.nfc.external.atr210.card;

import no.entur.android.nfc.external.atr210.schema.NfcAdpuTransmitRequest;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;

public class Atr210CardAdpuSynchronizedRequestMessage extends CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> {

    public Atr210CardAdpuSynchronizedRequestMessage(NfcAdpuTransmitRequest transmit, String requestTopic, String responseTopic) {
        super(responseTopic, transmit, requestTopic);
    }

}
