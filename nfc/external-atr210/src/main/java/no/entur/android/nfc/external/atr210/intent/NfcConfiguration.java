package no.entur.android.nfc.external.atr210.intent;

import android.os.Parcel;
import android.os.Parcelable;

public class NfcConfiguration implements Parcelable {

    private boolean enabled;
    private String hfId;
    private String hfName;
    private String samId;
    private String samName;

    public boolean isEnabled() {
        return enabled;
    }

    public String getHfId() {
        return hfId;
    }

    public String getHfName() {
        return hfName;
    }

    public String getSamId() {
        return samId;
    }

    public String getSamName() {
        return samName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHfId(String hfId) {
        this.hfId = hfId;
    }

    public void setHfName(String hfName) {
        this.hfName = hfName;
    }

    public void setSamId(String samId) {
        this.samId = samId;
    }

    public void setSamName(String samName) {
        this.samName = samName;
    }

    public NfcConfiguration(boolean enabled, String hfId, String hfName, String samId, String samName) {
        this.enabled = enabled;
        this.hfId = hfId;
        this.hfName = hfName;
        this.samId = samId;
        this.samName = samName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(enabled ? 1 : 0);
        dest.writeString(hfId);
        dest.writeString(hfName);
        dest.writeString(samId);
        dest.writeString(samName);
    }

    public static final Parcelable.Creator<NfcConfiguration> CREATOR = new Parcelable.Creator<NfcConfiguration>() {
        @Override
        public NfcConfiguration createFromParcel(Parcel in) {
            int enabled = in.readInt();
            String hfId = in.readString();
            String hfName = in.readString();
            String samId = in.readString();
            String samName = in.readString();

            return new NfcConfiguration(enabled == 1, hfId, hfName, samId, samName);
        }

        @Override
        public NfcConfiguration[] newArray(int size) {
            return new NfcConfiguration[size];
        }
    };

}