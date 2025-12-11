package no.entur.android.nfc.external.hwb.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/hf/apdu/response
public class NfcAdpuTransmitResponse extends AbstractResponse {

    private List<ApduResponse> result = new ArrayList<>();

    public List<ApduResponse> getResult() {
        return result;
    }

    public void setResult(List<ApduResponse> result) {
        this.result = result;
    }
}
