package no.entur.android.nfc.external.hwb.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/hf/status
public class ReadersStatusResponse extends AbstractResponse {

    // "hf_readers":
    @JsonProperty("hf_readers")
    private List<ReaderStatus> hfReaders = new ArrayList<>();

    @JsonProperty("sam_readers")
    private List<ReaderStatus> samReaders = new ArrayList<>();

    public List<ReaderStatus> getHfReaders() {
        return hfReaders;
    }

    public void setHfReaders(List<ReaderStatus> hfReaders) {
        this.hfReaders = hfReaders;
    }

    public List<ReaderStatus> getSamReaders() {
        return samReaders;
    }

    public void setSamReaders(List<ReaderStatus> samReaders) {
        this.samReaders = samReaders;
    }
}
