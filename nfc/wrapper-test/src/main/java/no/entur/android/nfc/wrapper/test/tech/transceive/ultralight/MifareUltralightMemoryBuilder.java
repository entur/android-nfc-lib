package no.entur.android.nfc.wrapper.test.tech.transceive.ultralight;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;

public class MifareUltralightMemoryBuilder {

    private static class Page {

        public Page(int index, byte[] content) {
            this.index = index;
            this.content = content;
        }

        int index;
        byte[] content;
    }

    private int size = 64;
    private byte[] tagId;
    private List<Page> pages = new ArrayList<>();

    public MifareUltralightMemoryBuilder withTagId(byte[] tagId) {
        this.tagId = tagId;
        return this;
    }

    public MifareUltralightMemoryBuilder withPageCount(int count) {
        size = count * 4;
        return this;
    }

    public MifareUltralightMemoryBuilder withByteCount(int size) {
        if(size % 4 != 0) {
            throw new IllegalStateException("Memory must be in pages of 4 bytes");
        }
        this.size = size;
        return this;
    }

    public MifareUltralightMemoryBuilder withPage(int index, byte[] content) {
        pages.add(new Page(index, content));
        return this;
    }

    public MockTransceive build() {
        byte[] memory = new byte[size];
        for (Page page : pages) {
            int offset = page.index * 4;
            if(offset + page.content.length > memory.length) {
                throw new IllegalStateException("Page " + page.index + " length " + page.content.length + " does not fit within memory of " + (size / 4) + " pages ("  + size + " bytes)");
            }
            System.arraycopy(page.content, 0, memory, offset, page.content.length);
        }

        if(tagId != null) {
            if(tagId.length != 7) {
                throw new IllegalStateException("Expected tag id 7 bytes");
            }
            System.arraycopy(tagId, 0, memory, 0, 3); // 0, 1, 2
            System.arraycopy(tagId, 3, memory, 3, 4); // 4, 5, 6, 7
        }

        return new MifareUltralightMockTransceive(memory);
    }


}
