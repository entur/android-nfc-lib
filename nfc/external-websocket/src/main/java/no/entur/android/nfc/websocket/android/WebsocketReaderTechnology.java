package no.entur.android.nfc.websocket.android;

import android.os.Parcelable;
import android.os.RemoteException;

import no.entur.android.nfc.external.service.tag.ReaderTechnology;
import no.entur.android.nfc.websocket.messages.card.CardClient;
import no.entur.android.nfc.wrapper.ErrorCodes;

// this is mostly a dummy implementation, the reader is remote; see also MinovaReaderTechnology

public class WebsocketReaderTechnology implements ReaderTechnology {

    /**
     * Default (and minimum) transceive timeout in milliseconds. The remote reader adds a network round trip, so
     * shorter timeouts are not applied.
     */

    public static final int DEFAULT_TIMEOUT = 1000;

    // assumed default value
    protected int maxTransieveLength = 253;

    // a single timeout for all technologies, as last set
    private volatile int timeout = DEFAULT_TIMEOUT;

    private volatile CardClient cardClient;

    /**
     * Set the card client of the current connection, which gets the timeout.
     *
     * @param cardClient card client, or null if not connected
     */

    public void setCardClient(CardClient cardClient) {
        this.cardClient = cardClient;

        apply();
    }

    @Override
    public int setTimeout(int technology, int timeout) throws RemoteException {
        this.timeout = timeout;

        apply();

        return ErrorCodes.SUCCESS;
    }

    private void apply() {
        CardClient cardClient = this.cardClient;
        if(cardClient != null) {
            cardClient.setTimeout(Math.max(timeout, DEFAULT_TIMEOUT));
        }
    }

    @Override
    public int getTimeout(int technology) throws RemoteException {
        return timeout;
    }

    @Override
    public void resetTimeouts() throws RemoteException {
        setTimeout(0, DEFAULT_TIMEOUT);
    }

    @Override
    public boolean canMakeReadOnly(int ndefType) throws RemoteException {
        return false;
    }

    @Override
    public int getMaxTransceiveLength(int technology) throws RemoteException {
        return maxTransieveLength;
    }

    @Override
    public boolean getExtendedLengthApdusSupported() throws RemoteException {
        return maxTransieveLength > 256;
    }

    @Override
    public int reconnect(int handle) throws RemoteException {
        return ErrorCodes.SUCCESS;
    }

    @Override
    public Parcelable transceiveMetadata(Parcelable parcelable) {
        return null;
    }
}
