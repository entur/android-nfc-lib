package no.entur.android.nfc.external.minova;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nfctools.api.TagType;

import static org.junit.Assert.*;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("no.entur.android.nfc.external.minova.test", appContext.getPackageName());
    }

    @Test
    public void convertHexAndGetTagTypeFromByteArray() {
        String uid = "3B8180018080";

        byte[] historicalBytes = hexStringToByteArray(uid);

        TagType type = TagType.identifyTagType(historicalBytes);

        Log.d("Test", "TagType is: " + type.getName() + ". " + ByteArrayHexStringConverter.toHexString(historicalBytes));
    }

    private static byte[] hexStringToByteArray(CharSequence s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

}