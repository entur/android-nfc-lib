package no.entur.android.nfc.external.minova.reader;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IMcr0XRemoteCommandWriter extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMcr0XRemoteCommandWriter.class);

    private final MinovaCommands commands;

    public IMcr0XRemoteCommandWriter(MinovaCommands commands) {
        this.commands = commands;
    }

/*	public byte[] getFirmware() {

		String firmware = null;
		Exception exception = null;
		try {
			firmware = commands.getFirmware(0);
		} catch (Exception e) {
			LOGGER.debug("Problem reading firmware", e);

			exception = e;
		}

		return returnValue(firmware, exception);
	}*/

    public byte[] getIp() {
        String ip = null;
        Exception exception = null;
        try {
            ip = commands.getIp();
        } catch (Exception e) {
            LOGGER.debug("Problem getting IP-address.", e);

            exception = e;
        }
        return returnValue(ip, exception);
    }

    public byte[] buzz(int durationInMillis, int times) {
        Exception exception = null;
        try {
            commands.buzz(durationInMillis, times);
        } catch (Exception e) {
            LOGGER.debug("Problem buzzing.", e);

            exception = e;
        }

        return returnValue(exception);
    }

    public byte[] displayText(int xAxis, int yAxis, int font, String text) {
        Exception exception = null;
        try {
            commands.displayText(xAxis, yAxis, font, text);
        } catch (Exception e) {
            LOGGER.debug("Problem displaying text.", e);

            exception = e;
        }

        return returnValue(exception);
    }

    public byte[] displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis) {
        Exception exception = null;
        try {
            commands.displayTextWithDuration(xAxis, yAxis, font, text, durationInMillis);
        } catch (Exception e) {
            LOGGER.debug("Problem displaying text with duration.", e);

            exception = e;
        }

        return returnValue(exception);
    }
}
