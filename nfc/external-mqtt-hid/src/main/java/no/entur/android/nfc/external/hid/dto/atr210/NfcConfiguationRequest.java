package no.entur.android.nfc.external.hid.dto.atr210;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// use empty request to read

// itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration/request
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"stability"})
public class NfcConfiguationRequest implements Parcelable {

    private Boolean enabled;

    @JsonProperty("hf_id")
    private String hfId;

    @JsonProperty("hf_name")
    private String hfName;

    @JsonProperty("sam_id")
    private String samId;

    @JsonProperty("sam_name")
    private String samName;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
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

    public int describeContents() {
        return 0;
    }
    
    public void writeToParcel(Parcel dest, int flags) {
        if(enabled == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(enabled ? 1 : 0);
        }
        dest.writeString(hfId);
        dest.writeString(hfName);
        dest.writeString(samId);
        dest.writeString(samName);
    }

    public static final Parcelable.Creator<NfcConfiguationRequest> CREATOR = new Parcelable.Creator<NfcConfiguationRequest>() {
        @Override
        public NfcConfiguationRequest createFromParcel(Parcel in) {
            NfcConfiguationRequest response = new NfcConfiguationRequest();

            int enabled = in.readInt();
            if (enabled != -1) {
                response.setEnabled(enabled == 1);
            }

            response.setHfId(in.readString());
            response.setHfName(in.readString());
            response.setSamId(in.readString());
            response.setSamName(in.readString());

            return response;
        }

        @Override
        public NfcConfiguationRequest[] newArray(int size) {
            return new NfcConfiguationRequest[size];
        }
    };

    @Override
    public String toString() {
        return "NfcConfiguationRequest{" +
                "enabled=" + enabled +
                ", hfId='" + hfId + '\'' +
                ", hfName='" + hfName + '\'' +
                ", samId='" + samId + '\'' +
                ", samName='" + samName + '\'' +
                '}';
    }

    @JsonIgnore
    public boolean isEmpty() {
        return enabled == null && hfId == null && hfName == null && samId == null && samName == null;
    }

}
