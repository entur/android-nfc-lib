package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

import android.os.Parcel;
import android.os.Parcelable;

public class CommandMetadataRequest implements Parcelable {

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Creator<CommandMetadataRequest> CREATOR = new Creator<CommandMetadataRequest>() {
        @Override
        public CommandMetadataRequest createFromParcel(Parcel in) {
            return new CommandMetadataRequest();
        }

        @Override
        public CommandMetadataRequest[] newArray(int size) {
            return new CommandMetadataRequest[size];
        }
    };

}
