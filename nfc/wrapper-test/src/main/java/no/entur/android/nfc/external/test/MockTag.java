package no.entur.android.nfc.external.test;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.entur.android.nfc.external.test.binder.DefaultINFcTagBinder;
import no.entur.android.nfc.external.test.binder.INFcTagBinder;
import no.entur.android.nfc.external.test.tech.MockBasicTagTechnology;
import no.entur.android.nfc.external.test.tech.MockBasicTagTechnologyImpl;
import no.entur.android.nfc.external.test.tech.MockIsoDep;
import no.entur.android.nfc.external.test.tech.MockMifareUltralight;
import no.entur.android.nfc.external.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.IsoDepImpl;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.MifareUltralightImpl;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class MockTag {

    public static final String ACTION_TAG_DISCOVERED = "no.entur.android.nfc.external.ExternalNfcTagCallback.action.TAG_DISCOVERED";
    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected static final byte[] EXTRA_ATQA_VALUE = new byte[] { 0x44, 0x03 };
    protected static final short EXTRA_SAK_VALUE = 0x20;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class IsoDepBuilder {

        private byte[] hiLayer = new byte[]{};
        private byte[] historicalBytes = new byte[]{};

        private MockTransceive transceive;

        public IsoDepBuilder withHiLayer(byte[] hiLayer) {
            this.hiLayer = hiLayer;
            return this;
        }

        public IsoDepBuilder withHistoricalBytes(byte[] historicalBytes) {
            this.historicalBytes = historicalBytes;
            return this;
        }

        public IsoDepBuilder withDesfireEV1() {
            this.historicalBytes = new byte[]{(byte) 0x80}; // TODO more specific
            return this;
        }

        public IsoDepBuilder withTransceive(MockTransceive mockTransceive) {
            this.transceive = mockTransceive;
            return this;
        }


        public MockIsoDep build() {
            return new MockIsoDep(hiLayer, historicalBytes, transceive);
        }
    }

    public static class MifareUltralightBuilder {

        private int type = MifareUltralight.TYPE_ULTRALIGHT;

        private MockTransceive transceive;

        public MifareUltralightBuilder withTransceive(MockTransceive mockTransceive) {
            this.transceive = mockTransceive;
            return this;
        }

        public MifareUltralightBuilder withType(int type) {
            this.type = type;
            return this;
        }

        public MockMifareUltralight build() {
            return new MockMifareUltralight(type, transceive);
        }
    }

    public static class Builder {

        private MockIsoDep isoDep;
        private MockMifareUltralight mifareUltralight;
        private Context context;
        private byte[] tagId;

        private int maxTransceiveLength = 1024;
        private boolean extendedLengthApdusSupported = true;
        private int timeout = 1000;

        private String action = ACTION_TAG_DISCOVERED;

        public Builder withTagId(byte[] tagId) {
            this.tagId = tagId;
            return this;
        }

        public Builder withRandomTagId() {
            this.tagId = randomTagId();
            return this;
        }

        private byte[] randomTagId() {
            byte[] tagId = new byte[7];
            Random random = new Random();
            random.nextBytes(tagId);
            tagId[0] = 0x04; // for NXP
           return tagId;
        }

        public Builder withTagId(String tagId) {
            this.tagId = MockBasicTagTechnologyImpl.hex(tagId);
            return this;
        }

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder withAction(String action) {
            this.action = action;
            return this;
        }

        public Builder withIsoDep(Consumer<IsoDepBuilder> isoDep) {
            IsoDepBuilder builder = new IsoDepBuilder();
            isoDep.accept(builder);
            this.isoDep = builder.build();
            return this;
        }

        public Builder withMifareUltralight(Consumer<MifareUltralightBuilder> consumer) {
            MifareUltralightBuilder builder = new MifareUltralightBuilder();
            consumer.accept(builder);
            this.mifareUltralight = builder.build();
            return this;
        }

        public Builder withIsoDep(MockIsoDep isoDep) {
            this.isoDep = isoDep;
            return this;
        }

        public Builder withMifareUltralight(MockMifareUltralight mifareUltralight) {
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
            if(isoDep != null && mifareUltralight != null) {
                throw new IllegalStateException();
            }
            if(tagId == null) {
                tagId = randomTagId();
            }

            DefaultINFcTagBinder binder = new DefaultINFcTagBinder();

            TagImpl tag = createTag(binder);

            List<MockBasicTagTechnology> technologies = new ArrayList<>();
            if(isoDep != null) {
                technologies.add(isoDep);
            }
            if(mifareUltralight != null) {
                technologies.add(mifareUltralight);
            }

            binder.setDelegate(new INFcTagBinder(technologies, maxTransceiveLength, extendedLengthApdusSupported, timeout, tag));

            return new MockTag(tag, context, binder, action);
        }

        private TagImpl createTag(DefaultINFcTagBinder binder) {
            if(isoDep != null) {
                return createIsoDepTag(binder);
            } else if(mifareUltralight != null) {
                return createMifareUltralightTag(binder);
            }
            throw new IllegalStateException();
        }

        private TagImpl createMifareUltralightTag(DefaultINFcTagBinder binder) {
            List<Bundle> bundles = new ArrayList<Bundle>();
            List<Integer> tech = new ArrayList<Integer>();

            if(mifareUltralight != null) {
                addNfcATechBundle(bundles, tech);
                addTechBundles(mifareUltralight, bundles, tech);
            }

            int[] techArray = new int[tech.size()];
            for (int i = 0; i < techArray.length; i++) {
                techArray[i] = tech.get(i);
            }

            return new TagImpl(tagId, techArray, bundles.toArray(new Bundle[bundles.size()]), binder.getServiceHandle(), binder);
        }

        private TagImpl createIsoDepTag(DefaultINFcTagBinder binder) {

            List<Bundle> bundles = new ArrayList<Bundle>();
            List<Integer> tech = new ArrayList<Integer>();

            if(isoDep != null) {
                addNfcATechBundle(bundles, tech);
                addDesfireTechBundle(isoDep, bundles, tech);
            }

            int[] techArray = new int[tech.size()];
            for (int i = 0; i < techArray.length; i++) {
                techArray[i] = tech.get(i);
            }

            return new TagImpl(tagId, techArray, bundles.toArray(new Bundle[bundles.size()]), binder.getServiceHandle(), binder);
        }

        protected void addNfcATechBundle(List<Bundle> bundles, List<Integer> tech) {
            Bundle nfcA = new Bundle();
            nfcA.putShort(NfcA.EXTRA_SAK, EXTRA_SAK_VALUE);
            nfcA.putByteArray(NfcA.EXTRA_ATQA, EXTRA_ATQA_VALUE);
            bundles.add(nfcA);
            tech.add(TagTechnology.NFC_A);
        }

        protected void addDesfireTechBundle(MockIsoDep isoDep, List<Bundle> bundles, List<Integer> tech) {
            Bundle desfire = new Bundle();
            desfire.putByteArray(IsoDepImpl.EXTRA_HIST_BYTES, isoDep.getHistoricalBytes());
            if (isoDep.getHiLayerResponse() != null) {
                desfire.putByteArray(IsoDepImpl.EXTRA_HI_LAYER_RESP, isoDep.getHiLayerResponse());
            }
            bundles.add(desfire);
            tech.add(TagTechnology.ISO_DEP);
        }

        protected void addTechBundles(MockMifareUltralight mifareUltralight, List<Bundle> bundles, List<Integer> tech) {
            Bundle ultralight = new Bundle();
            ultralight.putBoolean(MifareUltralightImpl.EXTRA_IS_UL_C, mifareUltralight.getType() == MifareUltralight.TYPE_ULTRALIGHT_C);
            bundles.add(ultralight);
            tech.add(TagTechnology.MIFARE_ULTRALIGHT);
        }

    }

    protected final TagImpl tag;
    protected final Context context;
    protected final DefaultINFcTagBinder binder;

    protected final String action;

    public MockTag(TagImpl tag, Context context, DefaultINFcTagBinder binder, String action) {
        this.tag = tag;
        this.context = context;
        this.binder = binder;
        this.action = action;
    }

    public void power() {
        binder.power();

        Intent intent = new Intent(action);

        intent.putExtra(NfcAdapter.EXTRA_TAG, tag);
        if (tag.getId() != null) {
            intent.putExtra(NfcAdapter.EXTRA_ID, tag.getId());
        }
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public Tag getTag() {
        return tag;
    }

    public void unpower() {
        binder.unpower();

        // TODO tag lost?
    }

}
