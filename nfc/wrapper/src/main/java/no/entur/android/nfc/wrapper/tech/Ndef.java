package no.entur.android.nfc.wrapper.tech;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.os.RemoteException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TagWrapper;

import java.io.IOException;

public abstract class Ndef implements BasicTagTechnology {

    public static final int NDEF_MODE_READ_ONLY = 1;

    public static final int NDEF_MODE_READ_WRITE = 2;

    public static final int NDEF_MODE_UNKNOWN = 3;

    public static final String EXTRA_NDEF_MSG = "ndefmsg";

    public static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";

    public static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";

    public static final String EXTRA_NDEF_TYPE = "ndeftype";

    public static final int TYPE_OTHER = -1;

    public static final int TYPE_1 = 1;

    public static final int TYPE_2 = 2;

    public static final int TYPE_3 = 3;

    public static final int TYPE_4 = 4;

    public static final int TYPE_MIFARE_CLASSIC = 101;

    public static final int TYPE_ICODE_SLI = 102;

    public static final String UNKNOWN = "android.ndef.unknown";
    /** NFC Forum Tag Type 1 */
    public static final String NFC_FORUM_TYPE_1 = "org.nfcforum.ndef.type1";
    /** NFC Forum Tag Type 2 */
    public static final String NFC_FORUM_TYPE_2 = "org.nfcforum.ndef.type2";
    /** NFC Forum Tag Type 4 */
    public static final String NFC_FORUM_TYPE_3 = "org.nfcforum.ndef.type3";
    /** NFC Forum Tag Type 4 */
    public static final String NFC_FORUM_TYPE_4 = "org.nfcforum.ndef.type4";
    /** NDEF on MIFARE Classic */
    public static final String MIFARE_CLASSIC = "com.nxp.ndef.mifareclassic";
    /**
     * NDEF on iCODE SLI
     */
    public static final String ICODE_SLI = "com.nxp.ndef.icodesli";


    /**
     * Get an instance of {@link NdefImpl} for the given tag.
     *
     * <p>Returns null if {@link NdefImpl} was not enumerated in {@link TagImpl#getTechList}.
     * This indicates the tag is not NDEF formatted, or that this tag
     * is NDEF formatted but under a vendor specification that this Android
     * device does not implement.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @param tag an NDEF compatible tag
     * @return Ndef object
     */
    public static Ndef get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.NDEF)) return null;
            try {
                return new NdefImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(delegate.getDelegate());
            if(ndef == null) {
                return null;
            }
            return new NdefWrapper(ndef);
        } else {
            throw new IllegalArgumentException();
        }
    }


    public abstract NdefMessage getCachedNdefMessage();

    public abstract String getType();

    public abstract int getMaxSize();

    public abstract boolean isWritable();

    public abstract NdefMessage getNdefMessage() throws IOException, FormatException;

    public abstract void writeNdefMessage(NdefMessage msg) throws IOException, FormatException;

    public abstract boolean canMakeReadOnly();

    public abstract boolean makeReadOnly() throws IOException;
}
