package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseReader;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseReaderFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;

public class ApduPartialTransceiveResponseReaderFactory implements PartialTransceiveResponseReaderFactory {

    protected final byte[] nextPartCommand;

    public ApduPartialTransceiveResponseReaderFactory(byte[] nextPartCommand) {
        if(nextPartCommand == null) {
            throw new IllegalArgumentException();
        }
        this.nextPartCommand = nextPartCommand;
    }

    @Override
    public PartialTransceiveResponseReader create(byte[] command, PartialTransceiveResponsePredicate predicate) {
        // ignore command, always response with the same command if partial response

        return new ApduPartialTransceiveResponseReader(nextPartCommand, predicate);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nextPartCommand.length);
        dest.writeByteArray(nextPartCommand);
    }

    public static final Parcelable.Creator<ApduPartialTransceiveResponseReaderFactory> CREATOR = new Parcelable.Creator<ApduPartialTransceiveResponseReaderFactory>() {
        @Override
        public ApduPartialTransceiveResponseReaderFactory createFromParcel(Parcel in) {
            int length = in.readInt();
            byte[] response = new byte[length];
            in.readByteArray(response);

            return new ApduPartialTransceiveResponseReaderFactory(response);
        }

        @Override
        public ApduPartialTransceiveResponseReaderFactory[] newArray(int size) {
            return new ApduPartialTransceiveResponseReaderFactory[size];
        }
    };

}
