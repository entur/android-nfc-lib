package no.entur.android.nfc.external.hwb.card;

import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.external.hwb.schema.NfcAdpuTransmitRequest;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;

public class Atr210CardAdpuSynchronizedRequestMessage extends CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> implements DeviceHwbMessage {

    public Atr210CardAdpuSynchronizedRequestMessage(NfcAdpuTransmitRequest transmit) {
        super(transmit.getTransceiveId(), transmit, "validators/nfc/apdu/" + transmit.getDeviceId() + "/transmit");
    }

    @Override
    public String getDeviceId() {
        return payload.getDeviceId();
    }
}
