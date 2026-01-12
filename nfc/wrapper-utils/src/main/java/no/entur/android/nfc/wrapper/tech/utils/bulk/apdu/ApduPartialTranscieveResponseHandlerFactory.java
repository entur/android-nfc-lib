package no.entur.android.nfc.wrapper.tech.utils.bulk.apdu;

import android.os.Parcel;
import android.os.Parcelable;

import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseHandler;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponseHandlerFactory;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTranscieveResponsePredicate;

public class ApduPartialTranscieveResponseHandlerFactory implements PartialTranscieveResponseHandlerFactory {

    protected final byte[] nextPartCommand;

    public ApduPartialTranscieveResponseHandlerFactory(byte[] nextPartCommand) {
        if(nextPartCommand == null) {
            throw new IllegalArgumentException();
        }
        this.nextPartCommand = nextPartCommand;
    }

    @Override
    public PartialTranscieveResponseHandler create(byte[] command, PartialTranscieveResponsePredicate predicate) {
        // ignore command, always response with the same command if partial response

        return new ApduPartialTranscieveResponseHandler(nextPartCommand, predicate);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nextPartCommand.length);
        dest.writeByteArray(nextPartCommand);
    }

    public static final Parcelable.Creator<ApduPartialTranscieveResponseHandlerFactory> CREATOR = new Parcelable.Creator<ApduPartialTranscieveResponseHandlerFactory>() {
        @Override
        public ApduPartialTranscieveResponseHandlerFactory createFromParcel(Parcel in) {
            int length = in.readInt();
            byte[] response = new byte[length];
            in.readByteArray(response);

            return new ApduPartialTranscieveResponseHandlerFactory(response);
        }

        @Override
        public ApduPartialTranscieveResponseHandlerFactory[] newArray(int size) {
            return new ApduPartialTranscieveResponseHandlerFactory[size];
        }
    };

}
