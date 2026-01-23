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

    private boolean partialTransceiveResponsePredicate;

    private boolean transceiveResponsePredicate;


    public CommandMetadataResponse(List<String> formats, boolean partialTransceiveResponsePredicate, boolean transceiveResponsePredicate) {
        this.formats = formats;
        this.partialTransceiveResponsePredicate = partialTransceiveResponsePredicate;
        this.transceiveResponsePredicate = transceiveResponsePredicate;
    }

    public CommandMetadataResponse() {
        this.formats = new ArrayList<>();
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

        dest.writeInt(transceiveResponsePredicate ? 1: 0 );
        dest.writeInt(partialTransceiveResponsePredicate ? 1 : 0);

    }

    public static final Creator<CommandMetadataResponse> CREATOR = new Creator<CommandMetadataResponse>() {
        @Override
        public CommandMetadataResponse createFromParcel(Parcel in) {
            int count = in.readInt();

            List<String> formats = new ArrayList<>(count);

            for(int i = 0; i < count; i++) {
                formats.add(in.readString());
            }

            boolean transceiveResponsePredicateType = in.readInt() == 1;
            boolean partialTransceiveResponsePredicateType = in.readInt() == 1;

            return new CommandMetadataResponse(formats, partialTransceiveResponsePredicateType, transceiveResponsePredicateType);
        }

        @Override
        public CommandMetadataResponse[] newArray(int size) {
            return new CommandMetadataResponse[size];
        }
    };

    public List<String> getFormats() {
        return formats;
    }

    public boolean isPartialTransceiveResponsePredicate() {
        return partialTransceiveResponsePredicate;
    }

    public boolean isTransceiveResponsePredicate() {
        return transceiveResponsePredicate;
    }
}
