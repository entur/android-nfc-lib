package no.entur.android.nfc.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTransceive implements Parcelable {

    final Parcelable mRequestData;

    public ParcelableTransceive(Parcelable data) {
        mRequestData = data;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRequestData, 0);
    }

    public static final Creator<ParcelableTransceive> CREATOR = new Creator<ParcelableTransceive>() {
        @Override
        public ParcelableTransceive createFromParcel(Parcel in) {
            Parcelable responseData = in.readParcelable(this.getClass().getClassLoader());
            return new ParcelableTransceive(responseData);
        }

        @Override
        public ParcelableTransceive[] newArray(int size) {
            return new ParcelableTransceive[size];
        }
    };


}
