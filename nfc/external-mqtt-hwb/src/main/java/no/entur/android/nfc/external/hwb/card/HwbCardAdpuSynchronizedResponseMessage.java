package no.entur.android.nfc.external.hwb.card;

import android.util.Base64;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import hwb.utilities.validators.nfc.apdu.receive.Result;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedResponseMessage;

public class HwbCardAdpuSynchronizedResponseMessage extends CardAdpuSynchronizedResponseMessage<UUID> implements DeviceHwbMessage {

    private final ReceiveSchema receive;

    public HwbCardAdpuSynchronizedResponseMessage(ReceiveSchema receive) {
        super(receive.getTransceiveId());

        this.receive = receive;
    }

    @Override
    public String getDeviceId() {
        return receive.getDeviceId();
    }

    public ReceiveSchema getPayload() {
        return receive;
    }

    @Override
    public byte[] getAdpu() throws IOException {
        List<Result> result = receive.getResult();
        if(result != null && !result.isEmpty()) {
            if(result.size() != 1) {
                throw new IOException("Expected single ADPU response, got " + result.size());
            }

            String frame = result.get(0).getFrame();
            if(frame != null && !frame.isEmpty()) {
                return Base64.decode(frame, 0);
            }
            throw new IOException("Empty response ADPU");
        }

        throw new IOException("No response ADPU");
    }
}
