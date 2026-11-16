package no.entur.android.nfc.external.acs.tag;

import android.content.Context;
import android.content.Intent;
import android.nfc.tech.MifareUltralight;

import org.nfctools.api.ApduTag;
import org.nfctools.api.TagType;
import org.nfctools.mf.MfException;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtag;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;
import org.nfctools.spi.acs.AcrMfUlReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.ReaderWrapper;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.IntentEnricher;
import no.entur.android.nfc.external.tag.MifareUltralightTagFactory;
import no.entur.android.nfc.external.tag.NfcADefaultCommandTechnology;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.INfcTag;

public class AcsMifareUltralightTagServiceSupport extends AbstractAcsMifareUltralightTagServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcsMifareUltralightTagServiceSupport.class);

    protected MifareUltralightTagFactory mifareUltralightTagFactory = new MifareUltralightTagFactory();

    protected TransceiveResultExceptionMapper exceptionMapper;

    public AcsMifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, boolean ntag21xUltralights, TransceiveResultExceptionMapper exceptionMapper) {
        super(context, tagService, store, ntag21xUltralights);

        this.exceptionMapper = exceptionMapper;
    }

    /**
     * Whether raw tag commands can be sent via the (PN53x InCommunicateThru) pass-through for this reader.
     * Also used for NTAG version detection, so that both stay in sync.
     *
     * @param readerName reader name
     * @return true if pass-through commands are supported
     */

    protected static boolean supportsPassthrough(String readerName) {
        return ReaderWrapper.supportsPassThrough(readerName);
    }

    /**
     * Classify MIFARE Ultralight vs Ultralight C the same way as Android does for the built-in NFC reader
     * (NativeNfcTag.isUltralightC(), based on NXP AN1303), so that both report the same type for the same tag.
     *
     * @param page2 page 2 (internal, internal, lock, lock)
     * @param page3 page 3 (OTP, or NDEF capability container)
     * @param page4 page 4 (version info if factory state), or null if not read
     * @return true if Ultralight C
     */

    protected static boolean isUltralightC(byte[] page2, byte[] page3, byte[] page4) {
        if (isBlank(page2, page3)) {
            // very likely a blank card, look at version info in page 4
            return page4 != null && page4[0] == (byte) 0x02 && page4[1] == (byte) 0x00;
        }
        if (page3[0] == (byte) 0xE1 && (page3[1] & 0xFF) < 0x20) {
            // NDEF capability container, the size of the data area is 0x06 for Ultralight, anything above indicates Ultralight C
            return (page3[2] & 0xFF) > 0x06;
        }
        // fall back to Ultralight
        return false;
    }

    /**
     * @param page2 page 2 (internal, internal, lock, lock)
     * @param page3 page 3 (OTP, or NDEF capability container)
     * @return true if lock bytes and OTP page are all zero, i.e. very likely a blank card
     */

    protected static boolean isBlank(byte[] page2, byte[] page3) {
        return page2[2] == 0 && page2[3] == 0 && page3[0] == 0 && page3[1] == 0 && page3[2] == 0 && page3[3] == 0;
    }

    @SuppressWarnings("java:S3776")
    public TagProxy mifareUltralight(int slotNumber, byte[] atr, TagType tagType, ApduTag acsTag, ACSIsoDepWrapper wrapper, String readerName, IntentEnricher extras) {
        List<TagTechnology> technologies = new ArrayList<>();

        Boolean canReadBlocks = null;
        try {
            // https://github.com/marshmellow42/proxmark3/commit/4745afb647c96a80f3f088f2afebf9686499680d

            MfUlReaderWriter readerWriter;

            Integer version = null;
            MfBlock[] initBlocks = null;
            if (ntag21xUltralights) {
                if (supportsPassthrough(readerName)) {
                    // detect via get version
                    try {
                        NfcNtag ntag = new NfcNtag(wrapper);

                        NfcNtagVersion ntagVersion = new NfcNtagVersion(ntag.getVersion());
                        version = ntagVersion.getType();

                        LOGGER.debug("Detected version " + version);
                    } catch (MfException e) {
                        LOGGER.debug("No version for Ultralight tag - non NTAG 21x-tag?", e);

                        broadcast(ExternalNfcTagCallback.ACTION_TECH_DISCOVERED);
                        return null;
                    }
                } else {
                    LOGGER.debug("Do not detect ntag version for this reader " + readerName);
                }
            }

            MfBlock[] capabilityBlock = null;
            if (version == null) {
                // LOGGER.debug("Detect tag via capability container");

                readerWriter = new AcrMfUlReaderWriter(acsTag);

                // detect via capability container
                // can't really see difference between outdated 203 and 213 tag or ultralight and 210 tag

                try {
                    // capability block at index 3
                    capabilityBlock = readerWriter.readBlock(3, 1);

                    version = getVersion(capabilityBlock[0]);

                    // LOGGER.debug("Detected version " + version);

                    canReadBlocks = true;
                } catch (Exception e) {
                    LOGGER.warn("Problem reading tag UID", e);

                    canReadBlocks = false;
                }
            }

            // init reader finally
            if (version != null) {
                if (version > 0) {
                    // readerWriter = new AcrMfUlNTAGReaderWriter(acsTag, new NfcNtag(reader, slotNumber), version);
                    readerWriter = new AcrMfUlReaderWriter(acsTag);
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
                    LOGGER.warn("Problem reading tag UID", e);

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

            int type;
            if (canReadBlocks) {
                // same classification as the built-in NFC reader (i.e. NTAG213, 215 and 216 with NDEF are Ultralight C)
                byte[] page2 = initBlocks[2].getData();
                byte[] page3 = initBlocks[3].getData();
                byte[] page4 = null;
                if (isBlank(page2, page3)) {
                    try {
                        page4 = readerWriter.readBlock(4, 1)[0].getData();
                    } catch (Exception e) {
                        LOGGER.debug("Unable to read page 4", e);
                    }
                }
                type = isUltralightC(page2, page3, page4) ? MifareUltralight.TYPE_ULTRALIGHT_C : MifareUltralight.TYPE_ULTRALIGHT;
            } else {
                type = MifareUltralight.TYPE_ULTRALIGHT_C;
            }

            if (canReadBlocks) {
                technologies.add(new AcsMifareUltralightCommandTechnology(readerWriter, exceptionMapper, supportsPassthrough(readerName)));
            }

            if (TechnologyType.isNFCA(atr)) {
                technologies.add(new NfcADefaultCommandTechnology(wrapper, false, exceptionMapper));
            }

            TagProxy tagProxy = store.add(slotNumber, technologies);

            Integer ntagVersion = version;
            Intent intent = mifareUltralightTagFactory.getTag(tagProxy.getHandle(), slotNumber, type, uid, atr, tagService, (i) -> {
                if (ntagVersion != null && ntagVersion > 0) {
                    i.putExtra(NfcNtag.EXTRA_ULTRALIGHT_TYPE, ntagVersion);
                }
                return i;
            });

            intent = extras.enrich(intent);

            LOGGER.debug("Broadcast mifare ultralight");

            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);

            tagProxy.setUid(uid);

            return tagProxy;
        } catch (Exception e) {
            LOGGER.debug("Problem reading from tag", e);
            TagUtility.sendTechBroadcast(context);
        }
        return null;
    }


}
