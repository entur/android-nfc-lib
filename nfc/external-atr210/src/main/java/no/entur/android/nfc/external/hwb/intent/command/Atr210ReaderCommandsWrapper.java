package no.entur.android.nfc.external.hwb.intent.command;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import no.entur.android.nfc.external.hwb.reader.Atr210ReaderCommands;
import no.entur.android.nfc.external.hwb.schema.NfcConfiguationRequest;
import no.entur.android.nfc.external.hwb.schema.NfcConfiguationResponse;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class Atr210ReaderCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderCommandsWrapper.class);

    protected final Atr210ReaderCommands atr210ReaderCommands;

    public Atr210ReaderCommandsWrapper(Atr210ReaderCommands atr210ReaderCommands) {
        this.atr210ReaderCommands = atr210ReaderCommands;
    }

    public byte[] setNfcReadersConfiguration(byte[] value, long timeout) throws RemoteException {
        if (atr210ReaderCommands == null) {
            return RemoteCommandWriter.noReaderException();
        }

        NfcConfiguationResponse result = null;
        Exception exception = null;
        try {
            Parcel parcel = new Parcel();
            NfcConfiguationRequest request = ;

            result = atr210ReaderCommands.setNfcReadersConfiguration(request, timeout);
        } catch (Exception e) {
            LOGGER.debug("Problem setting NFC readers configuration", e);

            exception = e;
        }
        return returnValue(result, exception);
    }

    public byte[] getNfcReaders(long timeout) throws RemoteException {
        if (atr210ReaderCommands == null) {
            return RemoteCommandWriter.noReaderException();
        }

        NfcConfiguationResponse result = null;
        Exception exception = null;
        try {
            NfcConfiguationRequest request;

            result = atr210ReaderCommands.getNfcReaders(timeout);
        } catch (Exception e) {
            LOGGER.debug("Problem getting NFC readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }

    public byte[] getNfcReadersConfiguration(long timeout) {
        if (atr210ReaderCommands == null) {
            return RemoteCommandWriter.noReaderException();
        }
        NfcConfiguationResponse result = null;
        Exception exception = null;
        try {
            result = atr210ReaderCommands.getNfcReadersConfiguration(timeout);
        } catch (Exception e) {
            LOGGER.debug("Problem getting NFC readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }
}
