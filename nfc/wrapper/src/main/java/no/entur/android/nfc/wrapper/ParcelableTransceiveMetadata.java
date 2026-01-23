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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRequestData, 0);
    }

    public static final Creator<ParcelableTransceiveMetadata> CREATOR = new Creator<ParcelableTransceiveMetadata>() {
        @Override
        public ParcelableTransceiveMetadata createFromParcel(Parcel in) {
            Parcelable requestData = in.readParcelable(this.getClass().getClassLoader());
            return new ParcelableTransceiveMetadata(requestData);
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
