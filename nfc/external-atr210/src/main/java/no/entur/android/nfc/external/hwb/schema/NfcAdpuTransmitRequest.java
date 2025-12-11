package no.entur.android.nfc.external.hwb.schema;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/hf/apdu/transmit
public class NfcAdpuTransmitRequest {

    private List<ApduCommand> commands = new ArrayList<>();

    public List<ApduCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<ApduCommand> commands) {
        this.commands = commands;
    }
}
