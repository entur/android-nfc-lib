package hwb.utilities.mqtt3.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Atr210ReaderConfiguration {

    private boolean enabled;

    @JsonProperty("hf_id")
    private String hfId;
    @JsonProperty("sam_id")
    private String samId;

    private int sequence;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getHfId() {
        return hfId;
    }

    public void setHfId(String hfId) {
        this.hfId = hfId;
    }

    public String getSamId() {
        return samId;
    }

    public void setSamId(String samId) {
        this.samId = samId;
    }
}
