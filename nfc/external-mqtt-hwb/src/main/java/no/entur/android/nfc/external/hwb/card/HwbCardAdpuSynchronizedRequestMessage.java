package no.entur.android.nfc.external.hwb.card;

import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;

public class HwbCardAdpuSynchronizedRequestMessage extends CardAdpuSynchronizedRequestMessageRequest<UUID, TransmitSchema> implements DeviceHwbMessage {

    public HwbCardAdpuSynchronizedRequestMessage(TransmitSchema transmit) {
        super(transmit.getTransceiveId(), transmit, "validators/nfc/apdu/" + transmit.getDeviceId() + "/transmit");
    }

    @Override
    public String getDeviceId() {
        return payload.getDeviceId();
    }
}
