package no.entur.android.nfc.external.tag.bulk.validate;

import android.os.Parcel;
import android.os.Parcelable;

public class NativeDesfireChunkedResponseValidator implements TransceiveResponseValidator {

    private final int status;

    public NativeDesfireChunkedResponseValidator(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public boolean isValid(byte[] response) {
        if(response.length < 1) {
            return false;
        }

        int status = response[response.length - 1] & 0xFF;
        return status == this.status;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    public static final Parcelable.Creator<NativeDesfireChunkedResponseValidator> CREATOR = new Parcelable.Creator<NativeDesfireChunkedResponseValidator>() {
        @Override
        public NativeDesfireChunkedResponseValidator createFromParcel(Parcel in) {
            int status = in.readInt();

            return new NativeDesfireChunkedResponseValidator(status);
        }

        @Override
        public NativeDesfireChunkedResponseValidator[] newArray(int size) {
            return new NativeDesfireChunkedResponseValidator[size];
        }
    };


}
