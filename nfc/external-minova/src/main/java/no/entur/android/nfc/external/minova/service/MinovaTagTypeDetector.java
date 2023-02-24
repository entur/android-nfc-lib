package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

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
        }

        if(!result.hasHistoricalBytes()) {
            // XXX
            result.setHistoricalBytes(new byte[]{});
        }

        return result;
    }


    public static byte[] getHistoricalBytes(byte[] ats) {

        // Number of bytes sent including the one telling number of bytes.
        int length = ats[0] & 0xf;

        int numOfHistoricalBytes = (length - 1) - 4;
        byte[] historicalBytes = new byte[numOfHistoricalBytes];
        System.arraycopy(ats, length - numOfHistoricalBytes, historicalBytes, 0, numOfHistoricalBytes);

        return historicalBytes;
    }
}
