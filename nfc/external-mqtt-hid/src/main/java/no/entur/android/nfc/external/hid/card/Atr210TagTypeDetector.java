package no.entur.android.nfc.external.hid.card;

import org.nfctools.api.TagType;
import org.nfctools.api.detect.DefaultTagTypeDetector;

public class Atr210TagTypeDetector extends DefaultTagTypeDetector {

    @Override
    protected TagType parseInitialData(byte[] historicalBytes, int offset, int length) {

        // workaround for seemingly incorrect ultralight ATR:
        // 3B8F8001804F0CA000000306030005000000006E <- found
        // 3B8F8001804F0CA0000003060300030000000068 <- should have been

        if(historicalBytes[offset + 5] == (byte)0x03) {
            int tagId = (historicalBytes[offset + 6] & 0xff) << 8 | (historicalBytes[offset + 7] & 0xff);

            if(tagId == 0x0005) {
                return TagType.MIFARE_ULTRALIGHT;
            }
        }
        return super.parseInitialData(historicalBytes, offset, length);
    }
}
