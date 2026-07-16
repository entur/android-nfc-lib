package no.entur.android.nfc.external.hwb.intent.command;

import android.os.Parcelable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.hwb.reader.HwbReaderCommands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public abstract class HwbReaderCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HwbReaderCommandsWrapper.class);

    protected abstract HwbReaderCommands getCommands();

    public byte[] diagnostics(long duration) {
        Parcelable value = null;
        Exception exception = null;
        try {
            value = getCommands().getDiagnostics(duration);
        } catch (Exception e) {
            LOGGER.debug("Problem diagnostics", e);

            exception = e;
        }

        return returnValue(value, exception);
    }

    public byte[] isPresent(long duration) {
        Boolean value = null;
        Exception exception = null;
        try {
            value = getCommands().isPresent(duration);
        } catch (Exception e) {
            LOGGER.debug("Problem present check", e);

            exception = e;
        }

        return returnValue(value, exception);
    }


    public byte[] setControlResult(String validity, String title, String description) {
        Exception exception = null;
        try {
             getCommands().setControlResult(validity, title, description);
        } catch (Exception e) {
            LOGGER.debug("Problem diagnostics", e);

            exception = e;
        }

        return returnValue(exception);
    }
}
