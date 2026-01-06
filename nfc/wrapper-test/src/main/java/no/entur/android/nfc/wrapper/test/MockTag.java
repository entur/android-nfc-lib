package no.entur.android.nfc.wrapper.test;

import android.os.Bundle;

import androidx.core.util.Consumer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.entur.android.nfc.wrapper.test.binder.DefaultINFcTagBinder;
import no.entur.android.nfc.wrapper.test.binder.INFcTagBinder;
import no.entur.android.nfc.wrapper.test.tech.MockBasicTagTechnology;
import no.entur.android.nfc.wrapper.test.tech.MockBasicTagTechnologyImpl;
import no.entur.android.nfc.wrapper.test.tech.MockIsoDep;
import no.entur.android.nfc.wrapper.test.tech.MockMifareUltralight;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceiveAdapter;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.ultralight.MifareUltralightMemoryBuilder;
import no.entur.android.nfc.wrapper.test.tech.transceive.ultralight.MifareUltralightMockTransceive;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.tech.IsoDepImpl;
import no.entur.android.nfc.wrapper.tech.MifareUltralight;
import no.entur.android.nfc.wrapper.tech.MifareUltralightImpl;
import no.entur.android.nfc.wrapper.tech.NfcA;
import no.entur.android.nfc.wrapper.tech.TagTechnology;

public class MockTag extends TagImpl {

    protected static final byte[] EXTRA_ATQA_VALUE = new byte[] { 0x44, 0x03 };
    protected static final short EXTRA_SAK_ISODEP_EV1_VALUE = 0x20;
    protected static final short EXTRA_SAK_ULTRALIGHT_VALUE = 0x00;

    protected static final int ISODEP_DEFAULT_MAX_TRANSCEIVE_LENGTH = 65279;
    protected static final int MIFARE_ULTRALIGHT_DEFAULT_MAX_TRANSCEIVE_LENGTH = 253;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class IsoDepBuilder {

        private byte[] hiLayer = new byte[]{};
        private byte[] historicalBytes = new byte[]{};

        private MockParcelableTransceive mockParcelableTransceive;

        public IsoDepBuilder withHiLayer(byte[] hiLayer) {
            this.hiLayer = hiLayer;
            return this;
        }

        public IsoDepBuilder withHistoricalBytes(byte[] historicalBytes) {
            this.historicalBytes = historicalBytes;
            return this;
        }

        public IsoDepBuilder withDesfireEV1() {
            this.historicalBytes = new byte[]{(byte) 0x80};
            return this;
        }

        public IsoDepBuilder withTransceive(MockTransceive mockTransceive) {
            this.mockParcelableTransceive = new MockParcelableTransceiveAdapter(mockTransceive);
            return this;
        }

        public IsoDepBuilder withTransceive(MockParcelableTransceive mockParcelableTransceive) {
            this.mockParcelableTransceive = mockParcelableTransceive;
            return this;
        }

        public MockIsoDep build() {
            return new MockIsoDep(hiLayer, historicalBytes, mockParcelableTransceive);
        }
    }

    public static class MifareUltralightBuilder {

        private int type = MifareUltralight.TYPE_ULTRALIGHT;

        private MockTransceive transceive;

        public MifareUltralightBuilder withTransceive(MockTransceive mockTransceive) {
            this.transceive = mockTransceive;
            return this;
        }

        public MifareUltralightBuilder withMemoryLayout(byte[] memory) {
            this.transceive = new MifareUltralightMockTransceive(memory);
            return this;
        }

        public MifareUltralightBuilder withMemoryLayout(Consumer<MifareUltralightMemoryBuilder> consumer) {
            MifareUltralightMemoryBuilder builder = new MifareUltralightMemoryBuilder();
            consumer.accept(builder);
            this.transceive = builder.build();
            return this;
        }

        public MifareUltralightBuilder withType(int type) {
            this.type = type;
            return this;
        }

        protected MockMifareUltralight build() {
            return new MockMifareUltralight(type, transceive);
        }
    }

    public static class Builder {

        private Random random = new SecureRandom();

        private MockIsoDep isoDep;
        private MockMifareUltralight mifareUltralight;
        private byte[] tagId;

        /** bytes */
        private int maxTransceiveLength = -1;
        private boolean extendedLengthApdusSupported = true;

        /** milliseconds */
        private int timeout = 618;

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
            random.nextBytes(tagId);
            tagId[0] = 0x04; // for NXP
           return tagId;
        }

        public Builder withTagId(String tagId) {
            this.tagId = MockBasicTagTechnologyImpl.hex(tagId);
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
                throw new IllegalStateException("Expected IsoDep or MifareUltralight, not both.");
            }
            if(isoDep == null && mifareUltralight == null) {
                throw new IllegalStateException("Expected IsoDep or MifareUltralight.");
            }

            if(tagId == null) {
                tagId = randomTagId();
            }

            if(maxTransceiveLength == -1) {
                if(mifareUltralight != null) {
                    maxTransceiveLength = MIFARE_ULTRALIGHT_DEFAULT_MAX_TRANSCEIVE_LENGTH;
                } else if(isoDep != null) {
                    maxTransceiveLength = ISODEP_DEFAULT_MAX_TRANSCEIVE_LENGTH;
                } else {
                    throw new RuntimeException();
                }
            }

            if(mifareUltralight != null) {
                // make sure memory and tag correspond for mifare ultralight
                MockTransceive transceive = mifareUltralight.getTransceive();
                if(transceive instanceof MifareUltralightMockTransceive) {
                    MifareUltralightMockTransceive mock = (MifareUltralightMockTransceive)transceive;
                    mock.setTagId(tagId);
                }
            }

            DefaultINFcTagBinder binder = new DefaultINFcTagBinder();

            MockTag tag = createTag(binder);

            List<MockBasicTagTechnology> technologies = new ArrayList<>();
            if(isoDep != null) {
                technologies.add(isoDep);
            }
            if(mifareUltralight != null) {
                technologies.add(mifareUltralight);
            }

            binder.setDelegate(new INFcTagBinder(technologies, maxTransceiveLength, extendedLengthApdusSupported, timeout, tag));

            return tag;
        }

        private MockTag createTag(DefaultINFcTagBinder binder) {
            if(isoDep != null) {
                return createIsoDepTag(binder);
            } else if(mifareUltralight != null) {
                return createMifareUltralightTag(binder);
            }
            throw new IllegalStateException();
        }

        private MockTag createMifareUltralightTag(DefaultINFcTagBinder binder) {
            List<Bundle> bundles = new ArrayList<Bundle>();
            List<Integer> tech = new ArrayList<Integer>();

            if(mifareUltralight != null) {
                addNfcATechBundle(EXTRA_SAK_ULTRALIGHT_VALUE, bundles, tech);
                addTechBundles(mifareUltralight, bundles, tech);
            }

            int[] techArray = new int[tech.size()];
            for (int i = 0; i < techArray.length; i++) {
                techArray[i] = tech.get(i);
            }

            return new MockTag(tagId, techArray, bundles.toArray(new Bundle[bundles.size()]), binder.getServiceHandle(), binder);
        }

        private MockTag createIsoDepTag(DefaultINFcTagBinder binder) {

            List<Bundle> bundles = new ArrayList<Bundle>();
            List<Integer> tech = new ArrayList<Integer>();

            if(isoDep != null) {
                addNfcATechBundle(EXTRA_SAK_ISODEP_EV1_VALUE, bundles, tech);
                addDesfireTechBundle(isoDep, bundles, tech);
            }

            int[] techArray = new int[tech.size()];
            for (int i = 0; i < techArray.length; i++) {
                techArray[i] = tech.get(i);
            }

            return new MockTag(tagId, techArray, bundles.toArray(new Bundle[bundles.size()]), binder.getServiceHandle(), binder);
        }

        protected void addNfcATechBundle(short sak, List<Bundle> bundles, List<Integer> tech) {
            Bundle nfcA = new Bundle();
            nfcA.putShort(NfcA.EXTRA_SAK, sak);
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

    protected DefaultINFcTagBinder binder;
    protected final int serviceHandle;

    public MockTag(byte[] id, int[] techList, Bundle[] techListExtras, int serviceHandle, DefaultINFcTagBinder tagService) {
        super(id, techList, techListExtras, serviceHandle, tagService);
        this.binder = tagService;
        this.serviceHandle = serviceHandle;
    }

    public void setBinder(DefaultINFcTagBinder binder) {
        this.binder = binder;
    }

    public void present() {
        binder.present();
    }

    public void lost() {
        binder.lost();
        // TODO tag lost?
    }

    public int getServiceHandle() {
        return serviceHandle;
    }

}
