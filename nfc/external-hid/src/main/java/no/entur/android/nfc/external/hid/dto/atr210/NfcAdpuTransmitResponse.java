package no.entur.android.nfc.external.hid.dto.atr210;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

// itxpt/ticketreader/{PROVIDER_ID}/nfc/hf/apdu/response
public class NfcAdpuTransmitResponse extends AbstractMessage implements Parcelable {

    private List<ApduResponse> result;

    public NfcAdpuTransmitResponse() {
        this(new ArrayList<>());
    }

    public NfcAdpuTransmitResponse(List<ApduResponse> result) {
        this.result = result;
    }

    public List<ApduResponse> getResult() {
        return result;
    }

    public void setResult(List<ApduResponse> result) {
        this.result = result;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result.size());
        for (ApduResponse apduResponse : result) {
            dest.writeInt(apduResponse.getCommandId());
            dest.writeString(apduResponse.getFrame());
            dest.writeString(apduResponse.getResponse());
        }
    }

    public static final Parcelable.Creator<NfcAdpuTransmitResponse> CREATOR = new Parcelable.Creator<NfcAdpuTransmitResponse>() {
        @Override
        public NfcAdpuTransmitResponse createFromParcel(Parcel in) {
            int count = in.readInt();

            List<ApduResponse> commands = new ArrayList<>();
            for(int i = 0; i < count; i++) {

                ApduResponse c = new ApduResponse();
                c.setCommandId(in.readInt());
                c.setFrame(in.readString());
                c.setResponse(in.readString());

                commands.add(c);
            }

            return new NfcAdpuTransmitResponse(commands);
        }

        @Override
        public NfcAdpuTransmitResponse[] newArray(int size) {
            return new NfcAdpuTransmitResponse[size];
        }
    };
}
