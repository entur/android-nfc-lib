
package no.entur.android.nfc.external.atr210.schema.heartbeat;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "physical",
    "virtual"
})
public class Memory {

    @JsonProperty("physical")
    private Physical physical;
    @JsonProperty("virtual")
    private Virtual virtual;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("physical")
    public Physical getPhysical() {
        return physical;
    }

    @JsonProperty("physical")
    public void setPhysical(Physical physical) {
        this.physical = physical;
    }

    @JsonProperty("virtual")
    public Virtual getVirtual() {
        return virtual;
    }

    @JsonProperty("virtual")
    public void setVirtual(Virtual virtual) {
        this.virtual = virtual;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
