package no.entur.android.nfc.external.tag.bulk.chunked;

import android.os.Parcel;
import android.os.Parcelable;

public class NativeDesfireChunkedTransceiveResponseHandler implements ChunkedTransceiveResponseHandler {

    private final int status;

    private final byte[] responseAdpu;

    public NativeDesfireChunkedTransceiveResponseHandler(int status, byte[] responseAdpu) {
        this.status = status;
        this.responseAdpu = responseAdpu;
    }

    public int getStatus() {
        return status;
    }

    public byte[] getResponseAdpu() {
        return responseAdpu;
    }

    @Override
    public boolean isChunked(byte[] response) {
        if(response.length < 1) {
            return false;
        }

        int status = response[response.length - 1] & 0xFF;
        return status == this.status;
    }

    @Override
    public byte[] nextChunkedCommandAdpu() {
        return responseAdpu;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);

        dest.writeInt(responseAdpu.length);
        dest.writeByteArray(responseAdpu, 0, responseAdpu.length);
    }

    public static final Parcelable.Creator<NativeDesfireChunkedTransceiveResponseHandler> CREATOR = new Parcelable.Creator<NativeDesfireChunkedTransceiveResponseHandler>() {
        @Override
        public NativeDesfireChunkedTransceiveResponseHandler createFromParcel(Parcel in) {
            int status = in.readInt();

            int frameLength = in.readInt();
            byte[] frame = new byte[frameLength];
            in.readByteArray(frame);

            return new NativeDesfireChunkedTransceiveResponseHandler(status, frame);
        }

        @Override
        public NativeDesfireChunkedTransceiveResponseHandler[] newArray(int size) {
            return new NativeDesfireChunkedTransceiveResponseHandler[size];
        }
    };

}
