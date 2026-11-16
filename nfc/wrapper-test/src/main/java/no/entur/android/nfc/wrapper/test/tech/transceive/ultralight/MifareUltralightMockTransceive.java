package no.entur.android.nfc.wrapper.test.tech.transceive.ultralight;

import java.io.IOException;

import no.entur.android.nfc.wrapper.test.tech.transceive.MockTransceive;

/**
 * Simple ultralight card simulator.
 *
 */

public class MifareUltralightMockTransceive implements MockTransceive {

    protected static final int PAGE_SIZE = 4;
    protected static final int PAGE_READS = 4;

    protected int pageCount;
    protected byte[] memory;

    public MifareUltralightMockTransceive(byte[] memory) {
        setMemory(memory);
    }

    public void setTagId(byte[] tagId) {
        writeTagId(tagId, memory);
    }

    /**
     * Write a 7-byte UID and its check bytes to MIFARE Ultralight / NTAG memory:
     * page 0 is UID0 UID1 UID2 BCC0, page 1 is UID3 UID4 UID5 UID6 and page 2 byte 0 is BCC1.
     *
     * @param tagId 7-byte UID
     * @param memory tag memory
     */

    public static void writeTagId(byte[] tagId, byte[] memory) {
        if(tagId.length != 7) {
            throw new IllegalStateException("Expected tag id 7 bytes");
        }
        if(memory.length < 8) {
            throw new IllegalStateException("Expected at least 2 pages of memory");
        }
        System.arraycopy(tagId, 0, memory, 0, 3); // 0, 1, 2
        memory[3] = (byte) (0x88 ^ tagId[0] ^ tagId[1] ^ tagId[2]); // BCC0, 0x88 is the cascade tag
        System.arraycopy(tagId, 3, memory, 4, 4); // 4, 5, 6, 7
        if(memory.length > 8) {
            memory[8] = (byte) (tagId[3] ^ tagId[4] ^ tagId[5] ^ tagId[6]); // BCC1
        }
    }

    public void setMemory(byte[] memory) {
        if(memory.length % 4 != 0) {
            throw new IllegalArgumentException();
        }
        this.memory = memory;
        this.pageCount = memory.length / 4;
    }

    @Override
    public byte[] transceive(byte[] data, boolean raw) throws IOException {
        // read
        // byte[] cmd = { 0x30, (byte) pageOffset };

        // write
        // byte[] cmd = new byte[data.length + 2];
        // cmd[0] = (byte) 0xA2;
        // cmd[1] = (byte) pageOffset;
        // System.arraycopy(data, 0, cmd, 2, data.length);

        if(!raw) {
            if ((data[0] & 0xFF) == 0x30) {
                int pageOffset = data[1] & 0xFF;

                if (pageOffset >= pageCount) {
                    throw new IOException("Page offset not within page count " + pageCount);
                }

                int memoryOffset = pageOffset * PAGE_SIZE;
                int length = Math.min(memory.length - memoryOffset, PAGE_SIZE * PAGE_READS);

                byte[] response = new byte[length];
                System.arraycopy(memory, memoryOffset, response, 0, length);
                return response;
            } else if ((data[0] & 0xFF) == 0xA2) {
                int pageOffset = data[1] & 0xFF;

                if (pageOffset >= pageCount) {
                    throw new IOException("Page offset not within page count " + pageCount);
                }

                int payloadLength = data.length - 2;

                int payloadPages = payloadLength / PAGE_SIZE;
                if (payloadLength % PAGE_SIZE != 0) {
                    payloadPages++;
                }

                if (pageOffset + payloadPages > pageCount) {
                    throw new IOException("Payload exceeds card capacity");
                }

                int memoryOffset = pageOffset * PAGE_SIZE;

                System.arraycopy(data, 2, memory, memoryOffset, payloadLength);

                return new byte[]{};
            }
        } else {
            // TODO support raw commands too

        }
        throw new IOException("Command not supported");
    }

}
