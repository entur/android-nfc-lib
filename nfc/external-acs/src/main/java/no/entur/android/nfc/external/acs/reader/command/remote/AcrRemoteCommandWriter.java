package no.entur.android.nfc.external.acs.reader.command.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.acs.reader.command.ACRCommands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public abstract class AcrRemoteCommandWriter extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcrRemoteCommandWriter.class);

    protected abstract ACRCommands getCommands();


    public byte[] power(int slotNum, int action) {
        byte[] value = null;
        Exception exception = null;
        try {
            value = getCommands().power(slotNum, action);
        } catch (Exception e) {
            LOGGER.debug("Problem power", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] setProtocol(int slotNum, int preferredProtocols) {
        Integer value = null;
        Exception exception = null;
        try {
            value = getCommands().setProtocol(slotNum, preferredProtocols);
        } catch (Exception e) {
            LOGGER.debug("Problem set protocol", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] getState(int slotNum) {
        Integer value = null;
        Exception exception = null;
        try {
            value = getCommands().getState(slotNum);
        } catch (Exception e) {
            LOGGER.debug("Problem get state", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] getNumSlots() {
        Integer value = null;
        Exception exception = null;
        try {
            value = getCommands().getNumSlots();
        } catch (Exception e) {
            LOGGER.debug("Problem get number of slots", e);

            exception = e;
        }

        return returnValue(value, exception);
    }



    public byte[] control(int slotNum, int controlCode, byte[] command) {
        byte[] value = null;
        Exception exception = null;
        try {
            value = getCommands().control(slotNum, controlCode, command);
        } catch (Exception e) {
            LOGGER.debug("Problem control", e);

            exception = e;
        }
        return returnValue(value, exception);
    }

    public byte[] transmit(int slotNum, byte[] command) {
        byte[] value = null;
        Exception exception = null;
        try {
            value = getCommands().transmit(slotNum, command);
        } catch (Exception e) {
            LOGGER.debug("Problem transmit", e);

            exception = e;
        }
        return returnValue(value, exception);
    }

}
