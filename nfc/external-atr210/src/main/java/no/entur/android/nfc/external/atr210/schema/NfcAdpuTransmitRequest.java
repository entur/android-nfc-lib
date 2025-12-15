package no.entur.android.nfc.external.atr210.schema;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/hf/apdu/transmit
public class NfcAdpuTransmitRequest implements Parcelable {

    private List<ApduCommand> commands;

    public NfcAdpuTransmitRequest(List<ApduCommand> commands) {
        this.commands = commands;
    }

    public NfcAdpuTransmitRequest() {
        this(new ArrayList<>());
    }

    public List<ApduCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<ApduCommand> commands) {
        this.commands = commands;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(commands.size());

        for (ApduCommand command : commands) {
            dest.writeInt(command.getCommandId());
            dest.writeString(command.getFrame());
        }
    }

    public static final Parcelable.Creator<NfcAdpuTransmitRequest> CREATOR = new Parcelable.Creator<NfcAdpuTransmitRequest>() {
        @Override
        public NfcAdpuTransmitRequest createFromParcel(Parcel in) {
            int count = in.readInt();

            List<ApduCommand> commands = new ArrayList<>();
            for(int i = 0; i < count; i++) {

                ApduCommand c = new ApduCommand();
                c.setCommandId(in.readInt());
                c.setFrame(in.readString());

                commands.add(c);
            }

            return new NfcAdpuTransmitRequest(commands);
        }

        @Override
        public NfcAdpuTransmitRequest[] newArray(int size) {
            return new NfcAdpuTransmitRequest[size];
        }
    };
}
