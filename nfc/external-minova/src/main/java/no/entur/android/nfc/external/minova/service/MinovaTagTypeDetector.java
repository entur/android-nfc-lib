package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import android.util.Log;

import androidx.annotation.NonNull;

import org.nfctools.api.TagType;

import java.io.IOException;

import no.entur.android.nfc.external.minova.reader.MinovaCommands;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaTagTypeDetector {

    public MinovaTagType getTagType(MinovaCommands commands) throws IOException, InterruptedException {
        // ATQ;SAK;ATS
        String response = commands.getType();

        return getTagType(response);
    }

    public MinovaTagType getTagType(String response) {
        // XXX too simple approach, use more info

        // ATQ;SAK;ATS
        // "0344;20;067577810280";

        // TODO is the ATQ reversed?

        // 20: 0010 0000
        int lastIndex = response.lastIndexOf(";");
        int firstIndex = response.indexOf(";");

        // NFC Forum Digital Specification 2.0 14.6.2.
        String ats = response.substring(lastIndex + 1);
        String sak = response.substring(firstIndex + 1, lastIndex);
        String atq = response.substring(0, firstIndex);

        MinovaTagType result = new MinovaTagType();

        result.setAts(hexStringToByteArray(ats));
        result.setAtq(hexStringToByteArray(atq));

        byte[] sakBytes = hexStringToByteArray(sak);
        result.setSak(sakBytes[0] & 0xFF);

        // see NXP document AN10833 - https://www.nxp.com/docs/en/application-note/AN10833.pdf
        // identify tag family, leave the details to the upper layer
        // TODO better match android tag technology categories

        switch(result.getSak()) {
            case 0x20: {
                result.setTagType(TagType.ISO_DEP);
                result.setHistoricalBytes(getHistoricalBytes(result.getAts()));
                break;
            }
            case 0x00: {
                // TODO this is inaccurate
                result.setTagType(TagType.MIFARE_ULTRALIGHT);
                break;
            }
            case 0x19: {
                result.setTagType(TagType.MIFARE_CLASSIC_1K);
                break;
            }
            case 0x09: {
                result.setTagType(TagType.MIFARE_MINI);
                break;
            }
            case 0x10: {
                result.setTagType(TagType.MIFARE_PLUS_SL2_2K);
                break;
            }
            case 0x11: {
                result.setTagType(TagType.MIFARE_PLUS_SL2_4K);
                break;
            }
            case 0x38: {
                // TODO this is inaccurate
                result.setTagType(TagType.MIFARE_CLASSIC_4K);
                break;
            }
            case 0x28: {
                // TODO this is inaccurate
                result.setTagType(TagType.MIFARE_CLASSIC_1K);
                break;
            }
            case 0x08: {
                // TODO this is inaccurate
                result.setTagType(TagType.MIFARE_PLUS_SL1_2K);
                break;
            }
            case 0x18: {
                // TODO this is inaccurate
                result.setTagType(TagType.MIFARE_PLUS_SL1_4K);
                break;
            }
            default: {
                // This "IsoDep first" approach is not accurate, but there is not much to loose at this point
                result.setTagType(TagType.ISO_DEP);
                result.setHistoricalBytes(getHistoricalBytes(result.getAts()));
                break;
            }
        }

        if(!result.hasHistoricalBytes()) {
            // XXX
            result.setHistoricalBytes(new byte[]{});
        }

        return result;
    }


    public static byte[] getHistoricalBytes(byte[] ats) {

        // ATS format: See NFC Forum Digital Specification 2.0 14.6.2.
        // The length byte TL is followed by a variable number of bytes in the following order:
        // - Format byte T0,
        // - Interface bytes TA(1), TB(1), TC(1), and
        // - Historical bytes T1 to Tk.
        //
        // Number of bytes sent including the one telling number of bytes.
        int length = ats[0] & 0xFF;
        if(length != ats.length || length <= 5) {
            return null;
        }

        int numOfHistoricalBytes = (length - 1) - 4;
        byte[] historicalBytes = new byte[numOfHistoricalBytes];
        System.arraycopy(ats, length - numOfHistoricalBytes, historicalBytes, 0, numOfHistoricalBytes);

        return historicalBytes;
    }
}
