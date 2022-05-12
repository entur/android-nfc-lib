package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import org.nfctools.api.TagType;

import no.entur.android.nfc.external.acs.service.AbstractService;
import no.entur.android.nfc.external.minova.reader.MinovaReaderWrapper;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public abstract class AbstractMinovaTcpService extends AbstractService {

    // No port below 1025 can be used in the linux system.
    private final int port = 1025;

    private MinovaReaderWrapper reader = new MinovaReaderWrapper(this::onTagPresent, port);

    @Override
    public void onCreate() {
        super.onCreate();
        reader.start();
    }

    private void onTagPresent(int slot, String uid) {
        String response = reader.sendCommandForResponse(slot, "GETTYPE");
        String atsString = response.substring((response.lastIndexOf(";") + 1));

        byte[] atr = getAtr(hexStringToByteArray(atsString));
        TagType tag = TagType.identifyTagType(atr);

        handleTag(tag, atr, uid, reader.clients.get(slot));
    }

    protected abstract void handleTag(TagType tag, byte[] atr, String uid, CommandInputOutputThread<String, String> reader);

    private static byte[] getAtr(byte[] ats) {
        // First byte of Smart cards ATR start with 3B or 3F, we use 3B. Next number should be 8.
        String atrString = "3B8";

        // Number of bytes sent including the one telling number of bytes.
        int length = ats[0] & 0xf;

        // ATS for DESFire starts with 0x75, saying that the next 3 bytes are interface bytes. The rest are historical bytes.
        int numOfHistoricalBytes = (length - 1) - 4;
        byte[] historicalBytes = new byte[numOfHistoricalBytes];
        System.arraycopy(ats, length - numOfHistoricalBytes, historicalBytes, 0, numOfHistoricalBytes);

        // The second byte's second nibble is number of historical bytes.
        atrString += numOfHistoricalBytes;

        // Third and fourth byte should be 0x80 0x01.
        atrString += "8001";

        // Next we add the actual historical bytes.
        atrString += ByteArrayHexStringConverter.toHexString(historicalBytes);

        byte[] atrWithoutChecksum = hexStringToByteArray(atrString);

        // Create checksum..
        byte[] checkSumBytes = new byte[atrWithoutChecksum.length - 1];
        System.arraycopy(atrWithoutChecksum, 1, checkSumBytes, 0, checkSumBytes.length);

        // Last byte of ATR is a checksum based on second through last byte before the checksum.
        atrString += createXorChecksum(checkSumBytes);

        System.out.println(atrString);

        // 0x3B + 0x8(numOfHistoricalBytes) + 0x80 0x01 + historicalBytes + checksum
        return hexStringToByteArray(atrString);
    }

    private static String createXorChecksum(byte[] bytes) {
        int checkSum = bytes[0];
        System.out.println(ByteArrayHexStringConverter.toHexString(bytes));

        for (int i = 1; i < bytes.length; i++) {
            checkSum ^= bytes[i];
        }
        String chk = Integer.toHexString(checkSum);
        chk = chk.substring(chk.lastIndexOf('f') + 1);

        return chk;
    }

}
