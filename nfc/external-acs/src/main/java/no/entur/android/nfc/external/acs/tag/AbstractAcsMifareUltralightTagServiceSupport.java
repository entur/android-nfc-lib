package no.entur.android.nfc.external.acs.tag;

import android.content.Context;

import com.acs.smartcard.ReaderException;

import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.ul.LockPage;
import org.nfctools.mf.ul.MemoryLayout;
import org.nfctools.mf.ul.MfUlReaderWriter;
import org.nfctools.mf.ul.ntag.NfcNtagVersion;

import java.io.IOException;

import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.tag.AbstractTagServiceSupport;
import no.entur.android.nfc.wrapper.INfcTag;

public abstract class AbstractAcsMifareUltralightTagServiceSupport extends AbstractTagServiceSupport {

    private static final String TAG = AbstractAcsMifareUltralightTagServiceSupport.class.getName();

    protected boolean ntag21xUltralights;

    public AbstractAcsMifareUltralightTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store, boolean ntag21xUltralights) {
        super(context, tagService, store);
        this.ntag21xUltralights = ntag21xUltralights;
    }

    public void setNtag21xUltralights(boolean ntag21xUltralights) {
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
