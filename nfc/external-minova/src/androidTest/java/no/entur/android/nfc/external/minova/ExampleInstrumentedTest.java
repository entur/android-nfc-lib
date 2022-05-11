package no.entur.android.nfc.external.minova;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nfctools.api.TagType;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import no.entur.android.nfc.external.minova.service.CommaCommandInput;
import no.entur.android.nfc.external.minova.service.CommaCommandOutput;
import no.entur.android.nfc.external.minova.service.MinovaService;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;
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
    public void getAtrFromAts() {
        byte[] ats = new byte[] { (byte) 0x06, (byte) 0x75, (byte) 0x77, (byte) 0x81, (byte) 0x02, (byte) 0x80};

        getAtr(ats);
    }

    @Test
    public void convertHexAndGetTagTypeFromByteArray() {
        String atsString = "067577810280";

        byte[] ats = hexStringToByteArray(atsString);
        byte[] atr = getAtr(ats);

        TagType type = TagType.identifyTagType(atr);

        Log.d("Test", "TagType is: " + type.getName() + ". " + ByteArrayHexStringConverter.toHexString(atr));
    }

    private static byte[] getAtr(byte[] ats) {
        // First byte of Smart cards ATR start with 3B or 3F, we use 3B. Next number should be 8.
        String atrString = "3B8";

        // Number of bytes sent including the one telling number of bytes.
        int length = ats[0] & 0xf;

        // ATS for DESFire starts with 0x75, saying that the next 3 bytes are interface bytes. The rest are historical bytes.
        int numOfHistoricalBytes = (length - 1) - 4;
        byte[] historicalBytes = new byte[numOfHistoricalBytes];
        System.arraycopy(ats, length-numOfHistoricalBytes, historicalBytes, 0, numOfHistoricalBytes);

        // The second byte's second nibble is number of historical bytes.
        atrString+=numOfHistoricalBytes;

        // Third and fourth byte should be 0x80 0x01.
        atrString+="8001";

        // Next we add the actual historical bytes.
        atrString+=ByteArrayHexStringConverter.toHexString(historicalBytes);

        byte[] atrWithoutChecksum = hexStringToByteArray(atrString);

        // Create checksum..
        byte[] checkSumBytes = new byte[atrWithoutChecksum.length-1];
        System.arraycopy(atrWithoutChecksum, 1, checkSumBytes, 0, checkSumBytes.length);

        // Last byte of ATR is a checksum based on second through last byte before the checksum.
        atrString += createXorChecksum(checkSumBytes);

        Log.d("Test", atrString);

        // 0x3B + 0x8(numOfHistoricalBytes) + 0x80 0x01 + historicalBytes + checksum
        return hexStringToByteArray(atrString);
    }

    private static byte[] hexStringToByteArray(CharSequence s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }
    private static String createXorChecksum(byte[] bytes) {
        int checkSum = bytes[0];
        Log.d("XorCheck", ByteArrayHexStringConverter.toHexString(bytes));

        for(int i = 1; i<bytes.length; i++) {
            checkSum ^= bytes[i];
        }
        String chk = Integer.toHexString(checkSum);
        chk = chk.substring(chk.lastIndexOf('f')+1);

        Log.d("Test", chk);

        return chk;
    }

}