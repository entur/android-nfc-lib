
package no.entur.android.nfc.external.hwb.schema.heartbeat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.entur.android.nfc.external.hwb.schema.AbstractMessage;

// itxpt/inventory/providers/{PROVIDER_ID}/heartbeat/relative
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeartbeatResponse extends AbstractMessage {

    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("deviceType")
    private String deviceType;
    @JsonProperty("intendedState")
    private String intendedState;
    @JsonProperty("macaddress")
    private String macaddress;
    @JsonProperty("memory")
    private Memory memory;
    @JsonProperty("network")
    private List<Network> network;
    @JsonProperty("nextHeartbeatWithinSeconds")
    private Double nextHeartbeatWithinSeconds;
    @JsonProperty("processor")
    private Processor processor;
    @JsonProperty("uptime")
    private Uptime uptime;
    @JsonProperty("version")
    private String version;

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @JsonProperty("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("deviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("deviceType")
    public String getDeviceType() {
        return deviceType;
    }

    @JsonProperty("deviceType")
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @JsonProperty("intendedState")
    public String getIntendedState() {
        return intendedState;
    }

    @JsonProperty("intendedState")
    public void setIntendedState(String intendedState) {
        this.intendedState = intendedState;
    }

    @JsonProperty("macaddress")
    public String getMacaddress() {
        return macaddress;
    }

    @JsonProperty("macaddress")
    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    @JsonProperty("memory")
    public Memory getMemory() {
        return memory;
    }

    @JsonProperty("memory")
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    @JsonProperty("network")
    public List<Network> getNetwork() {
        return network;
    }

    @JsonProperty("network")
    public void setNetwork(List<Network> network) {
        this.network = network;
    }

    @JsonProperty("nextHeartbeatWithinSeconds")
    public Double getNextHeartbeatWithinSeconds() {
        return nextHeartbeatWithinSeconds;
    }

    @JsonProperty("nextHeartbeatWithinSeconds")
    public void setNextHeartbeatWithinSeconds(Double nextHeartbeatWithinSeconds) {
        this.nextHeartbeatWithinSeconds = nextHeartbeatWithinSeconds;
    }

    @JsonProperty("processor")
    public Processor getProcessor() {
        return processor;
    }

    @JsonProperty("processor")
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    @JsonProperty("uptime")
    public Uptime getUptime() {
        return uptime;
    }

    @JsonProperty("uptime")
    public void setUptime(Uptime uptime) {
        this.uptime = uptime;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

}
