package no.entur.android.nfc.external.tag.bulk.validate;

import android.os.Parcel;
import android.os.Parcelable;

public class Iso7816TransceiveResponseValidator implements TransceiveResponseValidator {

    private final int sw1;
    private final int sw2;

    public Iso7816TransceiveResponseValidator(int sw1, int sw2) {
        this.sw1 = sw1;
        this.sw2 = sw2;
    }

    public int getSw1() {
        return sw1;
    }

    public int getSw2() {
        return sw2;
    }

    @Override
    public boolean isValid(byte[] response) {
        if(response.length < 2) {
            return false;
        }

        int sw1 = response[response.length - 1] & 0xFF;
        if(sw1 != this.sw1) {
            return false;
        }
        int sw2 = response[response.length - 2] & 0xFF;

        return this.sw2 == sw2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sw1);
        dest.writeInt(sw2);
    }

    public static final Parcelable.Creator<Iso7816TransceiveResponseValidator> CREATOR = new Parcelable.Creator<Iso7816TransceiveResponseValidator>() {
        @Override
        public Iso7816TransceiveResponseValidator createFromParcel(Parcel in) {
            int sw1 = in.readInt();
            int sw2 = in.readInt();

            return new Iso7816TransceiveResponseValidator(sw1, sw2);
        }

        @Override
        public Iso7816TransceiveResponseValidator[] newArray(int size) {
            return new Iso7816TransceiveResponseValidator[size];
        }
    };

}
