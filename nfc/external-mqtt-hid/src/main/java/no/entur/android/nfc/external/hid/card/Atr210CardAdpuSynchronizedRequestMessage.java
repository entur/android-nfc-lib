package no.entur.android.nfc.external.hid.card;

import java.util.List;

import no.entur.android.nfc.external.hid.dto.atr210.ApduCommand;
import no.entur.android.nfc.external.hid.dto.atr210.ApduResponse;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcAdpuTransmitResponse;
import no.entur.android.nfc.mqtt.messages.card.CardAdpuSynchronizedRequestMessageRequest;
import no.entur.android.nfc.mqtt.messages.sync.SynchronizedResponseMessage;

public class Atr210CardAdpuSynchronizedRequestMessage extends CardAdpuSynchronizedRequestMessageRequest<String, NfcAdpuTransmitRequest> {

    public Atr210CardAdpuSynchronizedRequestMessage(NfcAdpuTransmitRequest transmit, String requestTopic, String responseTopic) {
        super(responseTopic, transmit, requestTopic);
    }

    /**
     * All responses arrive on the same topic, so check the command id echoed by the reader, so that a late response
     * (i.e. after the previous request timed out) is not taken as the response to this request.
     */

    @Override
    public boolean isResponse(SynchronizedResponseMessage<String> response) {
        int commandId = getCommandId();
        if(commandId == 0 || !(response instanceof Atr210CardAdpuSynchronizedResponseMessage)) {
            return true;
        }
        NfcAdpuTransmitResponse payload = ((Atr210CardAdpuSynchronizedResponseMessage) response).getPayload();
        if(payload == null) {
            return true;
        }
        List<ApduResponse> result = payload.getResult();
        if(result == null || result.size() != 1) {
            return true;
        }
        int responseCommandId = result.get(0).getCommandId();

        // 0 if the reader does not echo the command id
        return responseCommandId == 0 || responseCommandId == commandId;
    }

    protected int getCommandId() {
        NfcAdpuTransmitRequest transmit = getPayload();
        if(transmit == null) {
            return 0;
        }
        List<ApduCommand> commands = transmit.getCommands();
        if(commands == null || commands.size() != 1) {
            return 0;
        }
        return commands.get(0).getCommandId();
    }
}
