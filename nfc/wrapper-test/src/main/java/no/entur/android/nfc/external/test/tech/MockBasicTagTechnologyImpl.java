package no.entur.android.nfc.external.test.tech;

import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;

/**
 * A base class for tag technologies that are built on top of transceive().
 */
public abstract class MockBasicTagTechnologyImpl extends MockBasicTagTechnology {

    private static final String LOG_TAG = MockBasicTagTechnologyImpl.class.getName();

    private MockTransceive transceive;

    public MockBasicTagTechnologyImpl(int tech, MockTransceive transceive) {
        super(tech);

        this.transceive = transceive;
    }

    public void setTransceive(MockTransceive transceive) {
        this.transceive = transceive;
    }

    public byte[] transceive(byte[] data) throws IOException {
        Log.d(LOG_TAG, " -> " + toHexString(data));
        byte[] response = this.transceive.transceive(data);
        Log.d(LOG_TAG, " <- " + toHexString(response));

        return response;
    }

    public static String toHexString(byte[] data) {
        return toHexString(data, "%02X");
    }

    public static String toHexString(byte[] data, String format) {
        StringBuilder sb = new StringBuilder();
        if (data != null) {
            for (byte b : data) {
                sb.append(String.format(format, b));
            }
        }
        return sb.toString();
    }

    public static byte[] hex(CharSequence s) {
        if(s.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }


}
