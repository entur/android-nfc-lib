package no.entur.android.nfc.external.hid.dto.atr210;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"stability"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbstractMessage {

    @JsonProperty("sequence")
    private int sequence;

    @JsonProperty("timestamp")
    private String timestamp;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
