package no.entur.android.nfc.external.minova.reader;

import android.util.Log;

import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class IMcr0XRemoteCommandWriter extends RemoteCommandWriter {

    private static final String TAG = IMcr0XRemoteCommandWriter.class.getName();

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
			Log.d(TAG, "Problem reading firmware", e);

			exception = e;
		}

		return returnValue(firmware, exception);
	}*/

    public byte[] buzz(int durationInMillis, int times) {
        Exception exception = null;
        try {
            commands.buzz(durationInMillis, times);
        } catch (Exception e) {
            Log.d(TAG, "Problem buzzing", e);

            exception = e;
        }

        return returnValue(exception);
    }

    public byte[] displayText(int xAxis, int yAxis, int font, String text) {
        Exception exception = null;
        try {
            commands.displayText(xAxis, yAxis, font, text);
        } catch (Exception e) {
            Log.d(TAG, "Problem buzzing", e);

            exception = e;
        }

        return returnValue(exception);
    }

    public byte[] displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis) {
        Exception exception = null;
        try {
            commands.displayTextWithDuration(xAxis, yAxis, font, text, durationInMillis);
        } catch (Exception e) {
            Log.d(TAG, "Problem buzzing", e);

            exception = e;
        }

        return returnValue(exception);
    }
}
