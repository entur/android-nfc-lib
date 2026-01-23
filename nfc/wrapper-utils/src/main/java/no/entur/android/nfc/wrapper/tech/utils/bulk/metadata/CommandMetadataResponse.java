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

    private boolean partialTranscieveResponsePredicate;

    private boolean transcieveResponsePredicate;


    public CommandMetadataResponse(List<String> formats, boolean partialTranscieveResponsePredicate, boolean transcieveResponsePredicate) {
        this.formats = formats;
        this.partialTranscieveResponsePredicate = partialTranscieveResponsePredicate;
        this.transcieveResponsePredicate = transcieveResponsePredicate;
    }

    public CommandMetadataResponse() {
        this.formats = new ArrayList<>();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(formats.size());

        for (String format : formats) {
            dest.writeString(format);
        }

        dest.writeInt(transcieveResponsePredicate ? 1: 0 );
        dest.writeInt(partialTranscieveResponsePredicate ? 1 : 0);

    }

    public static final Creator<CommandMetadataResponse> CREATOR = new Creator<CommandMetadataResponse>() {
        @Override
        public CommandMetadataResponse createFromParcel(Parcel in) {
            int count = in.readInt();

            List<String> formats = new ArrayList<>(count);

            for(int i = 0; i < count; i++) {
                formats.add(in.readString());
            }

            boolean transcieveResponsePredicateType = in.readInt() == 1;
            boolean partialTranscieveResponsePredicateType = in.readInt() == 1;

            return new CommandMetadataResponse(formats, partialTranscieveResponsePredicateType, transcieveResponsePredicateType);
        }

        @Override
        public CommandMetadataResponse[] newArray(int size) {
            return new CommandMetadataResponse[size];
        }
    };

}
