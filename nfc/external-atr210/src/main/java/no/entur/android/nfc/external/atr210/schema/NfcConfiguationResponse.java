package no.entur.android.nfc.external.atr210.schema;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/readers/configuration
public class NfcConfiguationResponse extends AbstractMessage implements Parcelable {

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

    public static final Parcelable.Creator<NfcConfiguationResponse> CREATOR = new Parcelable.Creator<NfcConfiguationResponse>() {
        @Override
        public NfcConfiguationResponse createFromParcel(Parcel in) {
            NfcConfiguationResponse response = new NfcConfiguationResponse();

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
        public NfcConfiguationResponse[] newArray(int size) {
            return new NfcConfiguationResponse[size];
        }
    };
}
