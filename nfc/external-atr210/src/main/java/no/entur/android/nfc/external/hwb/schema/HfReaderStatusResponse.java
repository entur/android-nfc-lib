package no.entur.android.nfc.external.hwb.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/hf/status
public class HfReaderStatusResponse extends AbstractResponse {

    // "hf_readers":
    @JsonProperty("hf_readers")
    private List<ReaderStatus> hfReaders = new ArrayList<>();

    public List<ReaderStatus> getHfReaders() {
        return hfReaders;
    }

    public void setHfReaders(List<ReaderStatus> hfReaders) {
        this.hfReaders = hfReaders;
    }

}
