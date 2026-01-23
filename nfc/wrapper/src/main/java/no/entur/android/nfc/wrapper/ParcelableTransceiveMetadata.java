package no.entur.android.nfc.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 *
 */

public class ParcelableTransceiveMetadata implements Parcelable {

    final Parcelable mRequestData;

    public ParcelableTransceiveMetadata(Parcelable data) {
        mRequestData = data;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRequestData, 0);
    }

    public static final Creator<ParcelableTransceiveMetadata> CREATOR = new Creator<ParcelableTransceiveMetadata>() {
        @Override
        public ParcelableTransceiveMetadata createFromParcel(Parcel in) {
            Parcelable responseData = in.readParcelable(this.getClass().getClassLoader());
            return new ParcelableTransceiveMetadata(responseData);
        }

        @Override
        public ParcelableTransceiveMetadata[] newArray(int size) {
            return new ParcelableTransceiveMetadata[size];
        }
    };

    public Parcelable getRequestData() {
        return mRequestData;
    }
}
