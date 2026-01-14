package no.entur.android.nfc.external.hid.test.tag;

import androidx.core.util.Consumer;

import java.security.SecureRandom;
import java.util.Random;

import no.entur.android.nfc.wrapper.test.tech.MockBasicTagTechnologyImpl;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockParcelableTransceiveAdapter;
import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;
import no.entur.android.nfc.wrapper.test.tech.transceive.ultralight.MifareUltralightMemoryBuilder;
import no.entur.android.nfc.wrapper.test.tech.transceive.ultralight.MifareUltralightMockTransceive;

public class Atr210MqttTag {

    protected static final int ISODEP_DEFAULT_MAX_TRANSCEIVE_LENGTH = 65279;
    protected static final int MIFARE_ULTRALIGHT_DEFAULT_MAX_TRANSCEIVE_LENGTH = 253;

    protected static final int DEFAULT_TIMEOUT = 618;

    private static final byte[] ISODEP_ATR = new byte[]{0x3B, (byte) 0x80, (byte) 0x80, 0x01, 0x01};

    private static final byte[] DESFIRE_EV1_ATR = new byte[]{0x3B, (byte) 0x81, (byte) 0x80,0x01, (byte) 0x80, (byte) 0x80};

    private static final byte[] ULTRALIGHT_ATR = new byte[]{0x3B, (byte) 0x8F, (byte) 0x80, 0x01, (byte) 0x80, 0x4F, 0x0C, (byte) 0xA0, 0x00, 0x00, 0x03, 0x06, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x68};

    public static Builder newBuilder() {
        return new Builder();
    }

    private enum Legend { // helper to set defaults
        EV1,
        HOST_CARD_EMULATION,
        ULTRALIGHT,
        NONE
    }

    public static class IsoDepBuilder {

        private MockParcelableTransceive mockParcelableTransceive;

        public IsoDepBuilder withTransceive(MockTransceive mockTransceive) {
            this.mockParcelableTransceive = new MockParcelableTransceiveAdapter(mockTransceive);
            return this;
        }

        public IsoDepBuilder withTransceive(MockParcelableTransceive mockParcelableTransceive) {
            this.mockParcelableTransceive = mockParcelableTransceive;
            return this;
        }

        public MockParcelableTransceive build() {
            return mockParcelableTransceive;
        }
    }

    public static class MifareUltralightBuilder {

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

        protected MockTransceive build() {
            return transceive;
        }
    }

    public static class Builder {

        private Random random = new SecureRandom();

        private MockTransceive mockTransceive;
        private byte[] tagId;

        private byte[] atr;

        /** bytes */
        private int maxTransceiveLength = -1;

        /** milliseconds */
        private int transceiveTimeout = -1;

        private Legend legend = Legend.NONE;

        public Builder withTagId(byte[] tagId) {
            this.tagId = tagId;
            return this;
        }

        public Builder withAtr(byte[] atr) {
            this.atr = atr;
            return this;
        }

        private byte[] getDefaultRandomTagId() {
            int size = legend == Legend.HOST_CARD_EMULATION ? 4 : 7;
            byte[] tagId = new byte[size];
            random.nextBytes(tagId);
            tagId[0] = 0x04; // for NXP
            return tagId;
        }

        public Builder withTagId(String tagId) {
            this.tagId = MockBasicTagTechnologyImpl.hex(tagId);
            return this;
        }

        public Builder withDesfireEV1(Consumer<IsoDepBuilder> isoDep) {
            IsoDepBuilder builder = new IsoDepBuilder();
            isoDep.accept(builder);
            this.mockTransceive = builder.build();
            this.legend = Legend.EV1;
            return this;
        }

        public Builder withHostCardEmulation(Consumer<IsoDepBuilder> mockTransceive) {
            IsoDepBuilder builder = new IsoDepBuilder();
            mockTransceive.accept(builder);
            this.mockTransceive = builder.build();
            this.legend = Legend.HOST_CARD_EMULATION;
            return this;
        }

        public Builder withMifareUltralight(Consumer<MifareUltralightBuilder> consumer) {
            MifareUltralightBuilder builder = new MifareUltralightBuilder();
            consumer.accept(builder);
            this.mockTransceive = builder.build();
            this.legend = Legend.ULTRALIGHT;
            return this;
        }

        public Builder withHostCardEmulation(MockTransceive mockTransceive) {
            this.mockTransceive = mockTransceive;
            this.legend = Legend.HOST_CARD_EMULATION;
            return this;
        }

        public Builder withDesfireEV1(MockTransceive isoDep) {
            this.mockTransceive = isoDep;
            this.legend = Legend.EV1;
            return this;
        }

        public Builder withMifareUltralight(MockTransceive mifareUltralight) {
            this.mockTransceive = mifareUltralight;
            this.legend = Legend.ULTRALIGHT;
            return this;
        }

        public Builder withMaxTransceiveLength(int maxTransceiveLength) {
            this.maxTransceiveLength = maxTransceiveLength;
            return this;
        }

        public Builder withTransceiveTimeout(int timeout) {
            this.transceiveTimeout = timeout;
            return this;
        }

        public Atr210MqttTag build() {
            if(mockTransceive == null) {
                throw new IllegalStateException("Expected IsoDep or MifareUltralight.");
            }

            if(tagId == null) {
                tagId = getDefaultRandomTagId();
            }

            if(atr == null) {
                atr = defaultAtr();
            }

            if(maxTransceiveLength == -1) {
                maxTransceiveLength = defaultMaxTransceiveLength();
            }

            if(transceiveTimeout == -1) {
                transceiveTimeout = DEFAULT_TIMEOUT;
            }

            if(mockTransceive instanceof MifareUltralightMockTransceive) {
                MifareUltralightMockTransceive mock = (MifareUltralightMockTransceive)mockTransceive;
                mock.setTagId(tagId);
            }

            return new Atr210MqttTag(mockTransceive, tagId, atr, maxTransceiveLength, transceiveTimeout);
        }

        private byte[] defaultAtr() {
            switch(legend) {
                case EV1: return DESFIRE_EV1_ATR;
                case ULTRALIGHT: return ULTRALIGHT_ATR;
                case HOST_CARD_EMULATION: return ISODEP_ATR;
            }
            throw new IllegalStateException("Expected ATR");
        }

        private int defaultMaxTransceiveLength() {
            switch(legend) {
                case EV1: return ISODEP_DEFAULT_MAX_TRANSCEIVE_LENGTH;
                case ULTRALIGHT: return MIFARE_ULTRALIGHT_DEFAULT_MAX_TRANSCEIVE_LENGTH;
                case HOST_CARD_EMULATION: return ISODEP_DEFAULT_MAX_TRANSCEIVE_LENGTH;
            }
            throw new IllegalStateException("Expected max transceive length");
        }

    }

    protected final MockTransceive impl;
    protected final byte[] tagId;
    protected final byte[] atr;

    protected int maxTransceiveLength;

    protected int transceiveTimeout;

    public Atr210MqttTag(MockTransceive impl, byte[] tagId, byte[] atr, int maxTransceiveLength, int transceiveTimeout) {
        this.impl = impl;
        this.tagId = tagId;
        this.atr = atr;
        this.maxTransceiveLength = maxTransceiveLength;
        this.transceiveTimeout = transceiveTimeout;
    }

    public MockTransceive getTagTechnology() {
        return impl;
    }

    public byte[] getTagId() {
        return tagId;
    }

    public byte[] getAtr() {
        return atr;
    }

    public int getMaxTransceiveLength() {
        return maxTransceiveLength;
    }

    public int getTransceiveTimeout() {
        return transceiveTimeout;
    }
}
