package no.entur.android.nfc.external.acs.tag;

import android.content.Context;
import android.content.Intent;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import com.acs.smartcard.ReaderException;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.LockPage;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.AbstractTagServiceSupport;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.wrapper.INfcTag;

public class MifareUltralightTagServiceSupport extends AbstractTagServiceSupport {

    private static final String TAG = MifareUltralightTagServiceSupport.class.getName();

    protected MifareUltralightTagFactory mifareUltralightTagFactory = new MifareUltralightTagFactory();

    protected final boolean ntag21xUltralights;

    public MifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, boolean ntag21xUltralights) {
        super(context, tagService, store);
        this.ntag21xUltralights = ntag21xUltralights;
    }

    protected int getVersion(MfBlock initBlock) {

        switch (initBlock.getData()[2]) {
            case 0x06: {
                if (!ntag21xUltralights) {
                    return -NfcNtagVersion.TYPE_NTAG210; // aka ultralight
                }
                return NfcNtagVersion.TYPE_NTAG210;
            }
            case 0x10: {
                return NfcNtagVersion.TYPE_NTAG212;
            }
            case 0x12: {
                if (!ntag21xUltralights) {
                    return -NfcNtagVersion.TYPE_NTAG213; // aka ultralight c
                }
                return NfcNtagVersion.TYPE_NTAG213;
            }
            case 0x3E: {
                return NfcNtagVersion.TYPE_NTAG215;
            }
            case 0x6D: {
                return NfcNtagVersion.TYPE_NTAG216;
            }
            case 0x6F: {
                return NfcNtagVersion.TYPE_NTAG216F;
            }
            default: {
                return 0;
            }
        }
    }

    private boolean isLocked(MfUlReaderWriter readerWriter, MemoryLayout memoryLayout) throws IOException, ReaderException {
        for (LockPage lockPage : memoryLayout.getLockPages()) {
            MfBlock[] block = readerWriter.readBlock(lockPage.getPage(), 1);
            for (int lockByte : lockPage.getLockBytes()) {
                if (block[0].getData()[lockByte] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("java:S3776")
    public void mifareUltralight(int slotNumber, byte[] atr, TagType tagType, ApduTag acsTag, ACSIsoDepWrapper wrapper, String readerName) {
        List<TagTechnology> technologies = new ArrayList<>();

        Boolean canReadBlocks = null;
        try {
            // https://github.com/marshmellow42/proxmark3/commit/4745afb647c96a80f3f088f2afebf9686499680d

            MfUlReaderWriter readerWriter;

            Integer version = null;
            MfBlock[] initBlocks = null;
            if (ntag21xUltralights) {
                if (!(readerName.contains("1255") || readerName.contains("1252"))) {
                    // detect via get version
                    try {
                        NfcNtag ntag = new NfcNtag(wrapper);

                        NfcNtagVersion ntagVersion = new NfcNtagVersion(ntag.getVersion());
                        version = ntagVersion.getType();

                        // Log.d(TAG, "Detected version " + version);
                    } catch (MfException e) {
                        Log.d(TAG, "No version for Ultralight tag - non NTAG 21x-tag?");

                        broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
                        return;
                    }
                }
            }

            MfBlock[] capabilityBlock = null;
            if (version == null) {
                // Log.d(TAG, "Detect tag via capability container");

                readerWriter = new AcrMfUlReaderWriter(acsTag);

                // detect via capability container
                // can't really see difference between outdated 203 and 213 tag or ultralight and 210 tag

                try {
                    // capability block at index 3
                    capabilityBlock = readerWriter.readBlock(3, 1);

                    version = getVersion(capabilityBlock[0]);

                    // Log.d(TAG, "Detected version " + version);

                    canReadBlocks = true;
                } catch (Exception e) {
                    Log.w(TAG, "Problem reading tag UID", e);

                    canReadBlocks = false;
                }
            }

            // init reader finally
            if (version != null) {
                if (version > 0) {
                    // readerWriter = new AcrMfUlNTAGReaderWriter(acsTag, new NfcNtag(reader, slotNumber), version);
                    readerWriter = new AcrMfUlReaderWriter(acsTag);

                    tagType = TagType.MIFARE_ULTRALIGHT_C;
                } else {
                    readerWriter = new AcrMfUlReaderWriter(acsTag);
                }
            } else {
                readerWriter = new AcrMfUlReaderWriter(acsTag);
            }

            if (canReadBlocks == null || canReadBlocks) {
                try {
                    if (capabilityBlock == null) {
                        initBlocks = readerWriter.readBlock(0, 4);
                    } else {
                        initBlocks = readerWriter.readBlock(0, 3);
                        initBlocks = new MfBlock[] { initBlocks[0], initBlocks[1], initBlocks[2], capabilityBlock[0] };
                    }
                    canReadBlocks = true;
                } catch (Exception e) {
                    Log.w(TAG, "Problem reading tag UID", e);

                    canReadBlocks = false;
                }
            }

            // get uid from first two blocks:
            // 3 bytes from index 0
            // 4 bytes from index 1

            byte[] uid;
            if (canReadBlocks) {
                uid = new byte[7];
                System.arraycopy(initBlocks[0].getData(), 0, uid, 0, 3);
                System.arraycopy(initBlocks[1].getData(), 0, uid, 3, 4);
            } else {
                uid = new byte[] { MifareUltralightTagFactory.NXP_MANUFACTURER_ID };
            }

            int type = MifareUltralight.TYPE_UNKNOWN;

            if (tagType == TagType.MIFARE_ULTRALIGHT_C || !canReadBlocks) {
                type = MifareUltralight.TYPE_ULTRALIGHT_C;
            }

            if (canReadBlocks) {
                technologies.add(new MifareUltralightAdapter(slotNumber, readerWriter));
            }

            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new PN532NfcAAdapter(slotNumber, wrapper, false));
                // technologies.add(new NfcAAdapter(slotNumber, reader, false));
            }

            int serviceHandle = store.add(slotNumber, technologies);

            Intent intent = mifareUltralightTagFactory.getTag(serviceHandle, slotNumber, type, version, uid, atr, tagService);

            Log.d(TAG, "Broadcast mifare ultralight");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
        } catch (Exception e) {
            Log.d(TAG, "Problem reading from tag", e);
            broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
        }
    }


}
