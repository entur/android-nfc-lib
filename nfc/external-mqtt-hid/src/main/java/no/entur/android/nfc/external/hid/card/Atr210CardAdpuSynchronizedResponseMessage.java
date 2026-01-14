package no.entur.android.nfc.external.hid.card;

import android.util.Base64;

import java.io.IOException;
import java.util.List;

import no.entur.android.nfc.external.hid.dto.atr210.ApduResponse;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedResponseMessage;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class Atr210CardAdpuSynchronizedResponseMessage extends CardAdpuSynchronizedResponseMessage<String> {

    private final NfcAdpuTransmitResponse response;

    public Atr210CardAdpuSynchronizedResponseMessage(String topic, NfcAdpuTransmitResponse response) {
        super(topic);

        this.response = response;
    }

    public NfcAdpuTransmitResponse getPayload() {
        return response;
    }

    @Override
    public byte[] getAdpu() throws IOException {
        List<ApduResponse> result = response.getResult();
        if(result != null && !result.isEmpty()) {
            if(result.size() != 1) {
                throw new IOException("Expected single ADPU response, got " + result.size());
            }

            String frame = result.get(0).getResponse();
            if(frame != null) {
                byte[] decode = ByteArrayHexStringConverter.hexStringToByteArray(frame);
                return decode;
            }
            throw new IOException("Empty response ADPU");
        }

        throw new IOException("No response ADPU");
    }
}
