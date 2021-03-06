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

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.acs.reader.command.ACSIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;
import no.entur.android.nfc.external.tag.AbstractTagServiceSupport;
import no.entur.android.nfc.external.tag.TechnologyType;
import no.entur.android.nfc.wrapper.INfcTag;

public abstract class AbstractMifareUltralightTagServiceSupport extends AbstractTagServiceSupport {

    private static final String TAG = AbstractMifareUltralightTagServiceSupport.class.getName();

    protected final boolean ntag21xUltralights;

    public AbstractMifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, boolean ntag21xUltralights) {
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

    protected boolean isLocked(MfUlReaderWriter readerWriter, MemoryLayout memoryLayout) throws IOException, ReaderException {
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

}
