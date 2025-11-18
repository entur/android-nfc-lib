package no.entur.android.nfc.external.hwb.reader;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.reader.CardAdpuRequestMessage;

public class HwbCardAdpuRequestMessage extends CardAdpuRequestMessage<String> implements DeviceHwbMessage {

    private final TransmitSchema transmit;

    public HwbCardAdpuRequestMessage(TransmitSchema transmit) {
        super(transmit.getTransceiveId().toString());

        this.transmit = transmit;
    }

    @Override
    public String getDeviceId() {
        return transmit.getDeviceId();
    }

    public TransmitSchema getPayload() {
        return transmit;
    }
}
