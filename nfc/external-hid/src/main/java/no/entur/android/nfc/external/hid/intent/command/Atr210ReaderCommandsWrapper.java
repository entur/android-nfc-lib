package no.entur.android.nfc.external.hid.intent.command;

import android.os.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.hid.intent.NfcConfiguration;
import no.entur.android.nfc.external.hid.intent.NfcTagReader;
import no.entur.android.nfc.external.hid.intent.NfcReaders;
import no.entur.android.nfc.external.hid.intent.NfcSamReader;
import no.entur.android.nfc.external.hid.reader.Atr210ReaderCommands;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationRequest;
import no.entur.android.nfc.external.hid.dto.atr210.NfcConfiguationResponse;
import no.entur.android.nfc.external.hid.dto.atr210.ReaderStatus;
import no.entur.android.nfc.external.hid.dto.atr210.ReadersStatusResponse;
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
