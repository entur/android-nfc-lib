package no.entur.android.nfc.external.hwb.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

// use empty request to read

// itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration/request
public class NfcConfiguationRequest {

    private boolean enabled;

    @JsonProperty("hf_id")
    private String hfId;

    @JsonProperty("hf_name")
    private String hfName;

    @JsonProperty("sam_id")
    private String samId;

    @JsonProperty("sam_name")
    private String samName;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHfId() {
        return hfId;
    }

    public void setHfId(String hfId) {
        this.hfId = hfId;
    }

    public String getHfName() {
        return hfName;
    }

    public void setHfName(String hfName) {
        this.hfName = hfName;
    }

    public String getSamId() {
        return samId;
    }

    public void setSamId(String samId) {
        this.samId = samId;
    }

    public String getSamName() {
        return samName;
    }

    public void setSamName(String samName) {
        this.samName = samName;
    }
}
