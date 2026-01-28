
package no.entur.android.nfc.external.hid.dto.atr210.heartbeat;

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
    "ipaddress",
    "name",
    "netmask",
    "type"
})
public class Network {

    @JsonProperty("ipaddress")
    private String ipaddress;
    @JsonProperty("name")
    private String name;
    @JsonProperty("netmask")
    private String netmask;
    @JsonProperty("type")
    private String type;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("ipaddress")
    public String getIpaddress() {
        return ipaddress;
    }

    @JsonProperty("ipaddress")
    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("netmask")
    public String getNetmask() {
        return netmask;
    }

    @JsonProperty("netmask")
    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
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
