package no.entur.android.nfc.external.hwb.reader;

import android.util.Base64;

import java.io.IOException;
import java.util.List;

import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import hwb.utilities.validators.nfc.apdu.receive.Result;
import no.entur.android.nfc.external.hwb.DeviceHwbMessage;
import no.entur.android.nfc.mqtt.messages.reader.CardAdpuResponseMessage;

public class HwbCardAdpuResponseMessage extends CardAdpuResponseMessage<String> implements DeviceHwbMessage {

    private final ReceiveSchema receive;

    public HwbCardAdpuResponseMessage(ReceiveSchema receive) {
        super(receive.getTransceiveId().toString());

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
            return Base64.decode(result.get(0).getFrame(), 0);
        }

        throw new IOException("No response ADPU");
    }
}
