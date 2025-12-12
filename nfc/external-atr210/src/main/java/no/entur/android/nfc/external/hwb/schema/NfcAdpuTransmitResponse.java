package no.entur.android.nfc.external.hwb.schema;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/hf/apdu/response
public class NfcAdpuTransmitResponse extends AbstractMessage {

    private List<ApduResponse> result = new ArrayList<>();

    public List<ApduResponse> getResult() {
        return result;
    }

    public void setResult(List<ApduResponse> result) {
        this.result = result;
    }
}
