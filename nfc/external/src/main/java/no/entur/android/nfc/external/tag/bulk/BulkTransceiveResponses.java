package no.entur.android.nfc.external.tag.bulk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BulkTransceiveResponses implements Parcelable {

    private List<TransceiveResponse> result;

    public BulkTransceiveResponses() {
        this(new ArrayList<>());
    }

    public BulkTransceiveResponses(List<TransceiveResponse> result) {
        this.result = result;
    }

    public List<TransceiveResponse> getResult() {
        return result;
    }

    public void setResult(List<TransceiveResponse> result) {
        this.result = result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result.size());
        for (TransceiveResponse transceiveResponse : result) {
            dest.writeInt(transceiveResponse.getId());

            byte[] frame = transceiveResponse.getFrame();
            dest.writeInt(frame.length);
            dest.writeByteArray(frame, 0, frame.length);
        }
    }

    public static final Creator<BulkTransceiveResponses> CREATOR = new Creator<BulkTransceiveResponses>() {
        @Override
        public BulkTransceiveResponses createFromParcel(Parcel in) {
            int count = in.readInt();

            List<TransceiveResponse> commands = new ArrayList<>();
            for(int i = 0; i < count; i++) {

                TransceiveResponse c = new TransceiveResponse();
                c.setId(in.readInt());

                int frameLength = in.readInt();
                byte[] frame = new byte[frameLength];
                in.readByteArray(frame);
                c.setFrame(frame);

                commands.add(c);
            }

            return new BulkTransceiveResponses(commands);
        }

        @Override
        public BulkTransceiveResponses[] newArray(int size) {
            return new BulkTransceiveResponses[size];
        }
    };
}
