package org.nfctools.api;

public interface TagTypeDetector<R> {

    default TagType parseAtr(R reader, byte[] atrBytes) {
        // match against known 'important types' first
        if(atrBytes.length == 6) {

            if(atrBytes[0] == (byte)0x3B &&
                    atrBytes[1] == (byte)0x81 &&
                    atrBytes[2] == (byte)0x80 &&
                    atrBytes[3] == (byte)0x01 &&
                    atrBytes[4] == (byte)0x80 &&
                    atrBytes[5] == (byte)0x80
            ) {
                return TagType.DESFIRE_EV1;
            }
        }

        ATR atr = new ATR(atrBytes);

        byte[] historicalBytes = atr.getHistoricalBytes();

        return parseHistoricalBytes(reader, historicalBytes);
    }

    TagType parseHistoricalBytes(R reader, byte[] historicalBytes);
}
