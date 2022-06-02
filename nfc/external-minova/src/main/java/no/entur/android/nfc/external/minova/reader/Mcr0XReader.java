package no.entur.android.nfc.external.minova.reader;

import android.os.Parcel;
import android.os.Parcelable;

class Mcr0XReader extends MinovaReader {

    private Mcr0XReader(String name) {
        this.name = name;
    }

    public static final Parcelable.Creator<Mcr0XReader> CREATOR = new Parcelable.Creator<Mcr0XReader>() {
        @Override
        public Mcr0XReader createFromParcel(Parcel in) {
            String name = in.readString();
            return new Mcr0XReader(name);
        }

        @Override
        public Mcr0XReader[] newArray(int size) {
            return new Mcr0XReader[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}
