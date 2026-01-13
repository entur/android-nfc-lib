package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseReader;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseReaderFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class ApduPartialTranscieveResponseReaderFactory implements PartialTranscieveResponseReaderFactory {

    protected final byte[] nextPartCommand;

    public ApduPartialTranscieveResponseReaderFactory(byte[] nextPartCommand) {
        if(nextPartCommand == null) {
            throw new IllegalArgumentException();
        }
        this.nextPartCommand = nextPartCommand;
    }

    @Override
    public PartialTranscieveResponseReader create(byte[] command, PartialTranscieveResponsePredicate predicate) {
        // ignore command, always response with the same command if partial response

        return new ApduPartialTranscieveResponseReader(nextPartCommand, predicate);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nextPartCommand.length);
        dest.writeByteArray(nextPartCommand);
    }

    public static final Parcelable.Creator<ApduPartialTranscieveResponseReaderFactory> CREATOR = new Parcelable.Creator<ApduPartialTranscieveResponseReaderFactory>() {
        @Override
        public ApduPartialTranscieveResponseReaderFactory createFromParcel(Parcel in) {
            int length = in.readInt();
            byte[] response = new byte[length];
            in.readByteArray(response);

            return new ApduPartialTranscieveResponseReaderFactory(response);
        }

        @Override
        public ApduPartialTranscieveResponseReaderFactory[] newArray(int size) {
            return new ApduPartialTranscieveResponseReaderFactory[size];
        }
    };

}
