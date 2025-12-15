package no.entur.android.nfc.external.tag.bulk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.tag.bulk.chunked.ChunkedTransceiveResponseHandler;
import no.entur.android.nfc.external.tag.bulk.validate.TransceiveResponseValidator;

/**
 *
 * A set of commands to preferably be executed by some kind of inline logic in the reader,
 * in order to do a more efficient card read.
 *
 * Response validation and chunk handling is best effort.
 *
 */

public class BulkTransceiveCommands implements Parcelable {

    private List<TransceiveCommand> commands;

    public BulkTransceiveCommands(List<TransceiveCommand> commands) {
        this.commands = commands;
    }

    public BulkTransceiveCommands() {
        this(new ArrayList<>());
    }

    public List<TransceiveCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<TransceiveCommand> commands) {
        this.commands = commands;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(commands.size());

        for (TransceiveCommand command : commands) {
            writeCommandToParcel(dest, command);
        }
    }

    protected void writeCommandToParcel(Parcel dest, TransceiveCommand command) {
        dest.writeInt(command.getId());

        byte[] frame = command.getFrame();
        dest.writeInt(frame.length);
        dest.writeByteArray(frame, 0, frame.length);

        ChunkedTransceiveResponseHandler chunkedTransceiveResponseHandler = command.getChunkedTransceiveResponseHandler();
        if(chunkedTransceiveResponseHandler != null) {
            dest.writeInt(1);
            dest.writeParcelable(chunkedTransceiveResponseHandler, 0);
        } else {
            dest.writeInt(0);
        }

        TransceiveResponseValidator transceiveResponseValidator = command.getTransceiveResponseValidator();
        if(transceiveResponseValidator != null) {
            dest.writeInt(1);
            dest.writeParcelable(transceiveResponseValidator, 0);
        } else {
            dest.writeInt(0);
        }

    }

    public static final Creator<BulkTransceiveCommands> CREATOR = new Creator<BulkTransceiveCommands>() {
        @Override
        public BulkTransceiveCommands createFromParcel(Parcel in) {
            int count = in.readInt();

            List<TransceiveCommand> commands = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                commands.add(readCommandFromParcel(in));
            }

            return new BulkTransceiveCommands(commands);
        }

        @Override
        public BulkTransceiveCommands[] newArray(int size) {
            return new BulkTransceiveCommands[size];
        }
    };

    private static TransceiveCommand readCommandFromParcel(Parcel in) {

        TransceiveCommand c = new TransceiveCommand();
        c.setId(in.readInt());

        int frameLength = in.readInt();
        byte[] frame = new byte[frameLength];
        in.readByteArray(frame);
        c.setFrame(frame);

        if(in.readInt() == 1) {
            c.setChunkedTransceiveResponseHandler(in.readParcelable(ChunkedTransceiveResponseHandler.class.getClassLoader()));
        }

        if(in.readInt() == 1) {
            c.setTransceiveResponseValidator(in.readParcelable(TransceiveResponseValidator.class.getClassLoader()));
        }

        return c;
    }
}
