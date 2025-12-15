package no.entur.android.nfc.external.tag.bulk.chunked;

import android.os.Parcel;
import android.os.Parcelable;

public class Iso7816ChunkedTransceiveResponseHandler implements ChunkedTransceiveResponseHandler {

    private final int sw1;
    private final int sw2;

    private final byte[] responseAdpu;

    public Iso7816ChunkedTransceiveResponseHandler(int sw1, int sw2, byte[] responseAdpu) {
        this.sw1 = sw1;
        this.sw2 = sw2;
        this.responseAdpu = responseAdpu;
    }

    public int getSw1() {
        return sw1;
    }

    public int getSw2() {
        return sw2;
    }

    public byte[] getResponseAdpu() {
        return responseAdpu;
    }

    @Override
    public boolean isChunked(byte[] response) {
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

    @Override
    public byte[] nextChunkedCommandAdpu() {
        return responseAdpu;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sw1);
        dest.writeInt(sw2);

        dest.writeInt(responseAdpu.length);
        dest.writeByteArray(responseAdpu, 0, responseAdpu.length);
    }

    public static final Parcelable.Creator<Iso7816ChunkedTransceiveResponseHandler> CREATOR = new Parcelable.Creator<Iso7816ChunkedTransceiveResponseHandler>() {
        @Override
        public Iso7816ChunkedTransceiveResponseHandler createFromParcel(Parcel in) {
            int sw1 = in.readInt();
            int sw2 = in.readInt();

            int frameLength = in.readInt();
            byte[] frame = new byte[frameLength];
            in.readByteArray(frame);

            return new Iso7816ChunkedTransceiveResponseHandler(sw1, sw2, frame);
        }

        @Override
        public Iso7816ChunkedTransceiveResponseHandler[] newArray(int size) {
            return new Iso7816ChunkedTransceiveResponseHandler[size];
        }
    };

}
