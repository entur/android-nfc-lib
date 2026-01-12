package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BulkTransceiveCommands implements Parcelable {

    private PartialTranscieveResponseHandlerFactory partialTranscieveResponseHandlerFactory;
    private PartialTranscieveResponsePredicate partialTranscieveResponsePredicate;

    private List<BulkTransceiveCommand> items = new ArrayList<>();

    public PartialTranscieveResponseHandlerFactory getPartialTranscieveResponseHandlerFactory() {
        return partialTranscieveResponseHandlerFactory;
    }

    public void setPartialTranscieveResponseHandlerFactory(PartialTranscieveResponseHandlerFactory partialTranscieveResponseHandlerFactory) {
        this.partialTranscieveResponseHandlerFactory = partialTranscieveResponseHandlerFactory;
    }

    public PartialTranscieveResponsePredicate getPartialTranscieveResponsePredicate() {
        return partialTranscieveResponsePredicate;
    }

    public void setPartialTranscieveResponsePredicate(PartialTranscieveResponsePredicate partialTranscieveResponsePredicate) {
        this.partialTranscieveResponsePredicate = partialTranscieveResponsePredicate;
    }

    public List<BulkTransceiveCommand> getItems() {
        return items;
    }

    public void setItems(List<BulkTransceiveCommand> items) {
        this.items = items;
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(items.size());
        for (BulkTransceiveCommand item : items) {

            dest.writeInt(item.getId());

            byte[] response = item.getCommand();
            dest.writeInt(response.length);
            dest.writeByteArray(response);

            PartialTranscieveResponsePredicate partialPredicate = item.getPartialTranscieveResponsePredicate();
            if(partialPredicate != null) {
                dest.writeInt(1);
                dest.writeParcelable(partialPredicate, 0);
            } else {
                dest.writeInt(0);
            }

            TranscieveResponsePredicate responsePredicate = item.getResponsePredicate();
            if(responsePredicate != null) {
                dest.writeInt(1);
                dest.writeParcelable(responsePredicate, 0);
            } else {
                dest.writeInt(0);
            }
        }
    }

    public static final Parcelable.Creator<BulkTransceiveCommands> CREATOR = new Parcelable.Creator<BulkTransceiveCommands>() {
        @Override
        public BulkTransceiveCommands createFromParcel(Parcel in) {

            BulkTransceiveCommands responses = new BulkTransceiveCommands();

            int count = in.readInt();

            for(int i = 0; i < count; i++) {

                BulkTransceiveCommand item = new BulkTransceiveCommand();

                int id = in.readInt();

                int length = in.readInt();
                byte[] response = new byte[length];
                in.readByteArray(response);

                if(in.readInt() == 1) {
                    item.setPartialTranscieveResponsePredicate(in.readParcelable(PartialTranscieveResponsePredicate.class.getClassLoader()));
                }

                if(in.readInt() == 1) {
                    item.setResponsePredicate(in.readParcelable(TranscieveResponsePredicate.class.getClassLoader()));
                }

                item.setCommand(response);
                item.setId(id);

                responses.add(item);

            }

            return responses;
        }

        @Override
        public BulkTransceiveCommands[] newArray(int size) {
            return new BulkTransceiveCommands[size];
        }
    };

    public void add(BulkTransceiveCommand item) {
        this.items.add(item);
    }
}
