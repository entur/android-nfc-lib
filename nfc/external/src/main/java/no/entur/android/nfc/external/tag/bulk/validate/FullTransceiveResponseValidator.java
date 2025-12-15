package no.entur.android.nfc.external.tag.bulk.validate;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class FullTransceiveResponseValidator implements TransceiveResponseValidator {

    private final byte[] response;

    public FullTransceiveResponseValidator(byte[] response) {
        this.response = response;
    }

    public byte[] getResponse() {
        return response;
    }

    @Override
    public boolean isValid(byte[] response) {
        return Arrays.equals(this.response, response);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        byte[] frame = response;
        dest.writeInt(frame.length);
        dest.writeByteArray(frame, 0, frame.length);
    }

    public static final Parcelable.Creator<FullTransceiveResponseValidator> CREATOR = new Parcelable.Creator<FullTransceiveResponseValidator>() {
        @Override
        public FullTransceiveResponseValidator createFromParcel(Parcel in) {
            int frameLength = in.readInt();
            byte[] frame = new byte[frameLength];
            in.readByteArray(frame);
            
            return new FullTransceiveResponseValidator(frame);
        }

        @Override
        public FullTransceiveResponseValidator[] newArray(int size) {
            return new FullTransceiveResponseValidator[size];
        }
    };

}
