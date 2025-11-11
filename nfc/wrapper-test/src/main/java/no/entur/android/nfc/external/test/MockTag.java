package no.entur.android.nfc.external.test;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.core.util.Consumer;

import no.entur.android.nfc.wrapper.INfcTag;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.IsoDep;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;

public class MockTag {

    public static final String ACTION_TAG_DISCOVERED = "no.entur.android.nfc.external.ExternalNfcTagCallback.action.TAG_DISCOVERED";
    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    public static Builder newBuilder() {
        return new Builder();
    }

    public Tag createTag(byte[] id, int[] techList, Bundle[] bundles, int serviceHandle, INfcTag tagService) {
        return new TagImpl(id, techList, bundles, serviceHandle, tagService);
    }

    public static class IsoDepBuilder {

        private byte[] hiLayer;
        private byte[] historicalBytes;

        private byte[] extraAtqa = new byte[] { 0x44, 0x03 };
        private short extraSak = 0x20;

        public IsoDep build() {
            return null;
        }
    }

    public static class Builder {

        private IsoDep isoDep;
        private MifareUltralight mifareUltralight;
        private Context context;
        private byte[] tagId;

        private int maxTransceiveLength = 1024;
        private boolean extendedLengthApdusSupported = true;
        private int timeout = 1000;

        public Builder withTagId(byte[] tagId) {
            this.tagId = tagId;
            return this;
        }

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder withIsoDep(Consumer<IsoDepBuilder> isoDep) {
            IsoDepBuilder builder = new IsoDepBuilder();
            isoDep.accept(builder);
            this.isoDep = builder.build();
            return this;
        }

        public Builder withIsoDep(IsoDep isoDep) {
            this.isoDep = isoDep;
            return this;
        }

        public Builder withMifareUltralight(MifareUltralight mifareUltralight) {
            this.mifareUltralight = mifareUltralight;
            return this;
        }

        public Builder withMaxTransceiveLength(int maxTransceiveLength) {
            this.maxTransceiveLength = maxTransceiveLength;
            return this;
        }

        public Builder withExtendedLengthApdusSupported(boolean extendedLengthApdusSupported) {
            this.extendedLengthApdusSupported = extendedLengthApdusSupported;
            return this;
        }

        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public MockTag build() {
            DefaultINFcTagBinder binder = new DefaultINFcTagBinder();

            TagImpl tag = null;

            return new MockTag(tag, context, binder);
        }
    }

    public MockTag(TagImpl tag, Context context, DefaultINFcTagBinder binder) {
        this.tag = tag;
        this.context = context;
        this.binder = binder;
    }

    private final TagImpl tag;
    private final Context context;
    private final DefaultINFcTagBinder binder;

    private boolean published = false;

    public void setPresent(boolean present) {
        if(!present) {
            binder.lost();
        } else {
            published = true;

            Intent intent = new Intent(ACTION_TAG_DISCOVERED);

            intent.putExtra(NfcAdapter.EXTRA_TAG, tag);
            if (tag.getId() != null) {
                intent.putExtra(NfcAdapter.EXTRA_ID, tag.getId());
            }
            context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
        }
    }

    public boolean isPresent() {
        return !binder.isLost();
    }

}
