package no.entur.android.nfc.wrapper.tech.utils.bulk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BulkTransceiveCommands implements Parcelable {

    // handler for partial responses.
    private List<PartialTransceiveResponseHandler> partialHandlers = new ArrayList<>();

    // commands
    private List<BulkTransceiveCommand> items = new ArrayList<>();

    public void setPartialHandlers(List<PartialTransceiveResponseHandler> partialHandlers) {
        this.partialHandlers = partialHandlers;
    }

    public List<PartialTransceiveResponseHandler> getPartialHandlers() {
        return partialHandlers;
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

            dest.writeString(item.getPartialHandlerId());

            TransceiveResponsePredicate responsePredicate = item.getResponsePredicate();
            if(responsePredicate != null) {
                dest.writeInt(1);
                dest.writeParcelable(responsePredicate, 0);
            } else {
                dest.writeInt(0);
            }
        }

        dest.writeInt(partialHandlers.size());
        for (PartialTransceiveResponseHandler item : partialHandlers) {
            dest.writeString(item.getId());
            dest.writeParcelable(item.getFactory(), 0);
            dest.writeParcelable(item.getPredicate(), 0);
        }

    }

    public static final Parcelable.Creator<BulkTransceiveCommands> CREATOR = new Parcelable.Creator<BulkTransceiveCommands>() {
        @Override
        public BulkTransceiveCommands createFromParcel(Parcel in) {

            BulkTransceiveCommands responses = new BulkTransceiveCommands();

            int commandCount = in.readInt();

            for(int i = 0; i < commandCount; i++) {

                BulkTransceiveCommand item = new BulkTransceiveCommand();

                int id = in.readInt();

                int length = in.readInt();
                byte[] response = new byte[length];
                in.readByteArray(response);

                String partialTransceiveResponseHandlerId = in.readString();

                item.setCommand(response);
                item.setId(id);
                item.setPartialHandlerId(partialTransceiveResponseHandlerId);

                if(in.readInt() == 1) {
                    item.setResponsePredicate(in.readParcelable(TransceiveResponsePredicate.class.getClassLoader()));
                }

                responses.add(item);
            }

            int partialHandlerCount = in.readInt();
            for(int i = 0; i < partialHandlerCount; i++) {
                String id = in.readString();
                PartialTransceiveResponseReaderFactory factory = in.readParcelable(PartialTransceiveResponseReaderFactory.class.getClassLoader());
                PartialTransceiveResponsePredicate predicate = in.readParcelable(PartialTransceiveResponsePredicate.class.getClassLoader());

                responses.add(new PartialTransceiveResponseHandler(id, predicate, factory));
            }

            return responses;
        }

        @Override
        public BulkTransceiveCommands[] newArray(int size) {
            return new BulkTransceiveCommands[size];
        }
    };

    private void add(PartialTransceiveResponseHandler handler) {
        this.partialHandlers.add(handler);
    }

    public void add(BulkTransceiveCommand item) {
        this.items.add(item);
    }
}
