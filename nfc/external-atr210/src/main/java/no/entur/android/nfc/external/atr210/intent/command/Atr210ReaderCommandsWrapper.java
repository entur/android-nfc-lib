package no.entur.android.nfc.external.atr210.intent.command;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.atr210.intent.NfcConfiguration;
import no.entur.android.nfc.external.atr210.intent.NfcTagReader;
import no.entur.android.nfc.external.atr210.intent.NfcReaders;
import no.entur.android.nfc.external.atr210.intent.NfcSamReader;
import no.entur.android.nfc.external.atr210.reader.Atr210ReaderCommands;
import no.entur.android.nfc.external.atr210.schema.NfcConfiguationRequest;
import no.entur.android.nfc.external.atr210.schema.NfcConfiguationResponse;
import no.entur.android.nfc.external.atr210.schema.ReaderStatus;
import no.entur.android.nfc.external.atr210.schema.ReadersStatusResponse;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class Atr210ReaderCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderCommandsWrapper.class);

    protected final Atr210ReaderCommands atr210ReaderCommands;

    public Atr210ReaderCommandsWrapper(Atr210ReaderCommands atr210ReaderCommands) {
        this.atr210ReaderCommands = atr210ReaderCommands;
    }

    public byte[] setNfcReadersConfiguration(byte[] request, long timeout) throws RemoteException {
        if (atr210ReaderCommands == null) {
            return RemoteCommandWriter.noReaderException();
        }

        NfcConfiguration result = null;
        Exception exception = null;
        try {
            NfcConfiguration nfcConfiguration = readParcelable(request, NfcConfiguration.CREATOR);

            NfcConfiguationRequest r = new NfcConfiguationRequest();
            r.setEnabled(nfcConfiguration.isEnabled());
            r.setHfId(nfcConfiguration.getHfId());
            r.setHfName(nfcConfiguration.getHfName());
            r.setSamId(nfcConfiguration.getSamId());
            r.setSamName(nfcConfiguration.getSamName());

            NfcConfiguationResponse response = atr210ReaderCommands.setNfcReadersConfiguration(r, timeout);

            result = new NfcConfiguration(
                    response.getEnabled() != null && response.getEnabled(),
                    response.getHfId(),
                    response.getHfName(),
                    response.getSamId(),
                    response.getSamName()
                 );
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

        NfcReaders result = null;
        Exception exception = null;
        try {
            ReadersStatusResponse response = atr210ReaderCommands.getNfcReaders(timeout);

            result = new NfcReaders();

            for (ReaderStatus samReader : response.getHfReaders()) {
                result.add(new NfcTagReader(samReader.getId(), samReader.getStatus(), samReader.getName()));
            }

            for (ReaderStatus samReader : response.getSamReaders()) {
                result.add(new NfcSamReader(samReader.getId(), samReader.getStatus(), samReader.getName()));
            }
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
        NfcConfiguration result = null;
        Exception exception = null;
        try {
            NfcConfiguationResponse response = atr210ReaderCommands.getNfcReadersConfiguration(timeout);

            result = new NfcConfiguration(
                    response.getEnabled() != null && response.getEnabled(),
                    response.getHfId(),
                    response.getHfName(),
                    response.getSamId(),
                    response.getSamName()
            );
        } catch (Exception e) {
            LOGGER.debug("Problem getting NFC readers", e);

            exception = e;
        }
        return returnValue(result, exception);
    }
}
