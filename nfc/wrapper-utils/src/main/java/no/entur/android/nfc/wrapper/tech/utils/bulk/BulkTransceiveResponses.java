package no.entur.android.nfc.wrapper.tech.utils.bulk;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BulkTransceiveResponses implements Parcelable {

    private List<BulkTransceiveResponse> items = new ArrayList<>();

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(items.size());
        for (BulkTransceiveResponse item : items) {

            dest.writeInt(item.getId());

            byte[] response = item.getResponse();
            dest.writeInt(response.length);
            dest.writeByteArray(response);
        }
    }

    public static final Parcelable.Creator<BulkTransceiveResponses> CREATOR = new Parcelable.Creator<BulkTransceiveResponses>() {
        @Override
        public BulkTransceiveResponses createFromParcel(Parcel in) {

            BulkTransceiveResponses responses = new BulkTransceiveResponses();

            int count = in.readInt();

            for(int i = 0; i < count; i++) {

                int id = in.readInt();

                int length = in.readInt();
                byte[] response = new byte[length];
                in.readByteArray(response);

                BulkTransceiveResponse item = new BulkTransceiveResponse();
                item.setResponse(response);
                item.setId(id);

                responses.add(item);

            }

            return responses;
        }

        @Override
        public BulkTransceiveResponses[] newArray(int size) {
            return new BulkTransceiveResponses[size];
        }
    };

    public void add(BulkTransceiveResponse item) {
        this.items.add(item);
    }

}
