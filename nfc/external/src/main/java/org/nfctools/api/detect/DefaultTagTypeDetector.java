package org.nfctools.api.detect;

import org.nfctools.api.TagType;

import java.io.ByteArrayOutputStream;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

/**
 * 'IsoDep first' type of tag type detector.
 *
 * @param <R>
 */

public class DefaultTagTypeDetector<R> implements TagTypeDetector<R> {

    /** Category indicator: historical bytes coded in non-TLV format */
    public static final byte CATEGORY_NON_TLV = (byte)0x00;
    /** Category indicator: historical bytes are a DIR data reference */
    public static final byte CATEGORY_DIR_DATA_REFERENCE = (byte)0x10;
    /** Category indicator: historical bytes coded in COMPACT-TLV */
    public static final byte CATEGORY_COMPACT_TLV = (byte)0x80;
    
    /** Tag for country data */
    public static final byte TAG_COUNTRY = (byte)0x10;
    /** Tag for issuer identification number (IIN) */
    public static final byte TAG_ISSUER = (byte)0x20;
    /** Coding for card service data (CSD) */
    public static final byte TAG_CARD_SERVICE_DATA = (byte)0x30;
    /** Tag for initial access data (IAD) retrieval information */
    public static final byte TAG_INITIAL_ACCESS_DATA = (byte)0x40;
    /** Tag for card issuer data (proprietary coding) */
    public static final byte TAG_CARD_ISSUER_DATA = (byte)0x50;
    /** Tag for pre-issuing data (proprietary coding) */
    public static final byte TAG_PRE_ISSUING_DATA = (byte)0x60;
    /** Tag for card capability bytes (SFT1, optional SFT2 and SFT3) */
    public static final byte TAG_CARD_CAPABILITIES = (byte)0x70;
    /** Tag for status indicator (LCS, SW or LCS+SW) */
    public static final byte TAG_STATUS_INDICATOR = (byte)0x80;

    /* Life cycle status */

    public static final byte LCS_UNKNOWN = (byte)0x00;
    public static final byte LCS_CREATION = (byte)0x01;
    public static final byte LCS_INITIALIZATION = (byte)0x03;
    public static final byte LCS_OPERATIONAL_DEACTIVATED = (byte)0x04;
    public static final byte LCS_OPERATIONAL_ACTIVATED = (byte)0x05;
    public static final byte LCS_TERMINATED = (byte)0x0C;

    /* Card service data */

    public static final byte CSD_SELECT_BY_DFNAME_FULL = (byte)0x80;
    public static final byte CSD_SELECT_BY_DFNAME_PARTIAL = (byte)0x40;
    public static final byte CSD_DO_IN_EF_DIR = (byte)0x20;
    public static final byte CSD_DO_IN_EF_ATR = (byte)0x10;
    public static final byte CSD_READ_MASK = (byte)0x08;
    public static final byte CSD_READ_RECORD = (byte)0x00;
    public static final byte CSD_READ_BINARY = (byte)0x08;
    public static final byte CSD_RESERVED_MASK = (byte)0x07;
    public static final byte CSD_RESERVED_OKAY = (byte)0x00;

    /* Card capabilities (Software function table 1) */

    public static final byte SFT1_DF_SELECT_BY_DFNAME_FULL = (byte)0x80;
    public static final byte SFT1_DF_SELECT_BY_DFNAME_PARTIAL = (byte)0x40;
    public static final byte SFT1_DF_SELECT_BY_PATH = (byte)0x20;
    public static final byte SFT1_DF_SELECT_BY_FILEID = (byte)0x10;
    public static final byte SFT1_DF_SELECT_IMPLICIT = (byte)0x08;
    public static final byte SFT1_EF_SFI_SUPPORTED = (byte)0x04;
    public static final byte SFT1_EF_RECORD_NUMBER_SUPPORTED = (byte)0x02;
    public static final byte SFT1_EF_RECORD_IDENTIFIER_SUPPORTED = (byte)0x01;

    /* Card capabilities (Software function table 2) */

    public static final byte SFT2_WRITE_MASK = (byte)0x60;
    public static final byte SFT2_WRITE_ONCE = (byte)0x00;
    public static final byte SFT2_WRITE_PROPRIETARY = (byte)0x20;
    public static final byte SFT2_WRITE_OR = (byte)0x40;
    public static final byte SFT2_WRITE_AND = (byte)0x60;
    public static final byte SFT2_DUSIZE_MASK = (byte)0x07;
    public static final byte SFT2_RESERVED_MASK = (byte)0x91;
    public static final byte SFT2_RESERVED_OKAY = (byte)0x00;

    /* Card capabilities (Software function table 3) */

    public static final byte SFT3_EXTENDED_APDU = (byte)0x40;
    public static final byte SFT3_CHANNELS_MASK = (byte)0x18;
    public static final byte SFT3_CHANNELS_UNSUPPORTED = (byte)0x00;
    public static final byte SFT3_CHANNELS_ASSIGNED_BY_CARD = (byte)0x08;
    public static final byte SFT3_CHANNELS_ASSIGNED_BY_HOST = (byte)0x10;
    public static final byte SFT3_CHANNELS_RESERVED = (byte)0x18;
    public static final byte SFT3_MAX_CHANNELS_MASK = (byte)0x03;
    public static final byte SFT3_RESERVED_MASK = (byte)0xA4;
    public static final byte SFT3_RESERVED_OKAY = (byte)0x00;
    
    @Override
    public TagType parseHistoricalBytes(R reader, byte[] historicalBytes) {
        if(historicalBytes != null && historicalBytes.length > 0) {
            if (historicalBytes[0] == CATEGORY_COMPACT_TLV) {
                // The coding of the COMPACT-TLV data objects is deduced from the basic encoding rules af ASN.1 (see ISO/IEC 8825 and annex D) for BER-TLV data objects with tag='4X' and length='0Y'. The coding of such data objects is replaced by 'XY' followed by 'Y' bytes of data. In this clause, 'X' is referred to as the tag number and 'Y' as the length.
                // Besides the data objects defined in this clause, the historical bytes may contain data objects defined in part 4 of ISO/IEC 7816. In this case the coding of the tags and length fields defined in part 5 shall be modified as above.
                // When COMPACT-TLV data objects defined in this clause appear in the ATR file, they shall be encoded according to the basic encoding rules of ASN.1 (i.e tag='4X', length='0Y').

                // the encoding returned from the ACS reader seems not to correspond with
                // the documentation and code examples for some reason.

                if (isCompactTlv(historicalBytes, 1, historicalBytes.length - 2)) {
                    int limit = historicalBytes.length;
                    int offset = 1;
                    while (offset < limit) {
                        int tag = historicalBytes[offset] & 0xF0;
                        int objLen = historicalBytes[offset] & 0xF;

                        if (tag == TAG_INITIAL_ACCESS_DATA) {
                            TagType result = parseInitialData(historicalBytes, offset + 1, objLen);
                            if (result != null) {
                                return result;
                            }
                        }
                        offset += 1 + objLen;
                    }
                } else {
                    // TLV? seems to be the case in
                    int limit = historicalBytes.length;
                    int offset = 1;
                    while (offset < limit) {
                        int tag = historicalBytes[offset] & 0xF0;
                        offset++;
                        int objLen = historicalBytes[offset] & 0xF;

                        // add sanity check
                        if (offset + 1 + objLen > limit) {
                            break;
                        }
                        offset++;
                        if (tag == TAG_INITIAL_ACCESS_DATA) {
                            TagType result = parseInitialData(historicalBytes, offset, objLen);
                            if (result != null) {
                                return result;
                            }
                        }
                        offset += objLen;
                    }
                }
            } else {
                // TODO parse more types

                // for ACS:
                // Use the APDU “FF CA 01 00 00h” to distinguish the ISO 14443A-4 and ISO 14443B-4 PICCs,
                // and retrieve the full ATS if available. ISO 14443A-3 or ISO 14443B-3/4 PICCs do have ATS returned.
            }
        }

        return TagType.ISO_DEP;
    }

    // add sanity check
    private boolean isCompactTlv(byte[] historicalBytes, int offset, int limit) {
        while(offset < limit) {
            int objLen = historicalBytes[offset] & 0xF;

            if(offset + 1 + objLen > limit) {
                return false;
            }
            offset += 1 + objLen;
        }
        return true;
    }

    protected TagType parseInitialData(byte[] historicalBytes, int offset, int length) {

        // https://stackoverflow.com/questions/23404314/determine-card-type-from-atr

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(historicalBytes, offset, length);
        System.out.println(ByteArrayHexStringConverter.toHexString(bout.toByteArray()));

        bout = new ByteArrayOutputStream();
        bout.write(historicalBytes, offset + 6, length - 6);
        System.out.println(ByteArrayHexStringConverter.toHexString(bout.toByteArray()));

        int tagId = (historicalBytes[offset + 6] & 0xff) << 8 | (historicalBytes[offset + 7] & 0xff);

        switch (tagId) {
            case 0x0001:
                return TagType.MIFARE_CLASSIC_1K;
            case 0x0002:
                return TagType.MIFARE_CLASSIC_4K;
            case 0x0003:
                return TagType.MIFARE_ULTRALIGHT;
            case 0x0026:
                return TagType.MIFARE_MINI;
            case 0x003A:
                return TagType.MIFARE_ULTRALIGHT_C;
            case 0x0036:
                return TagType.MIFARE_PLUS_SL1_2K;
            case 0x0037:
                return TagType.MIFARE_PLUS_SL1_4K;
            case 0x0038:
                return TagType.MIFARE_PLUS_SL2_2K;
            case 0x0039:
                return TagType.MIFARE_PLUS_SL2_4K;
            case 0x0030:
                return TagType.TOPAZ_JEWEL;
            case 0xFF40:
                return TagType.NFCIP;
            case 0xFF88:
                return TagType.INFINEON_MIFARE_SLE_1K;
            default: {

            }
        }

        if(historicalBytes[offset + 6] == (byte)0xFF) {
            // assume android device
            return TagType.ISO_14443_TYPE_A;
        }

        return null;
    }
}
