package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Simple command metadata format.
 *
 * TODO: Support more fine-grained capabilities.
 *
 */

public class CommandMetadataResponse implements Parcelable {

    public static final String COMMAND_FORMAT_ISO7816 = "iso7816";
    public static final String COMMAND_FORMAT_NATIVE_MIFARE_DESFIRE = "nativeMifareDesfire";

    // frame formats
    private List<String> formats;

    private BulkTransceiveMetadata bulkTransceiveMetadata;

    public CommandMetadataResponse(List<String> formats, BulkTransceiveMetadata bulkTransceiveMetadata) {
        this.formats = formats;
        this.bulkTransceiveMetadata = bulkTransceiveMetadata;
    }

    public CommandMetadataResponse() {
        this.formats = new ArrayList<>();
    }

    public CommandMetadataResponse(List<String> formats) {
        this.formats = formats;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(formats.size());

        for (String format : formats) {
            dest.writeString(format);
        }

        dest.writeInt(bulkTransceiveMetadata != null ? 1: 0 );
        if(bulkTransceiveMetadata != null) {
            dest.writeInt(1);
            dest.writeInt(bulkTransceiveMetadata.isResponsePredicate() ? 1: 0 );
            dest.writeInt(bulkTransceiveMetadata.isPartialResponsePredicate() ? 1 : 0);
        } else {
            dest.writeInt(0);
        }


    }

    public static final Creator<CommandMetadataResponse> CREATOR = new Creator<CommandMetadataResponse>() {
        @Override
        public CommandMetadataResponse createFromParcel(Parcel in) {
            int count = in.readInt();

            List<String> formats = new ArrayList<>(count);

            for(int i = 0; i < count; i++) {
                formats.add(in.readString());
            }

            BulkTransceiveMetadata bulkTransceiveMetadata = null;

            if(in.readInt() == 1) {

                boolean transceiveResponsePredicateType = in.readInt() == 1;
                boolean partialTransceiveResponsePredicateType = in.readInt() == 1;

                bulkTransceiveMetadata = new BulkTransceiveMetadata(partialTransceiveResponsePredicateType, transceiveResponsePredicateType);
            } else {
                bulkTransceiveMetadata = null;
            }

            return new CommandMetadataResponse(formats, bulkTransceiveMetadata);
        }

        @Override
        public CommandMetadataResponse[] newArray(int size) {
            return new CommandMetadataResponse[size];
        }
    };

    public List<String> getFormats() {
        return formats;
    }

    public BulkTransceiveMetadata getBulkMetadata() {
        return bulkTransceiveMetadata;
    }

    public void setBulkMetadata(BulkTransceiveMetadata bulkTransceiveMetadata) {
        this.bulkTransceiveMetadata = bulkTransceiveMetadata;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}
