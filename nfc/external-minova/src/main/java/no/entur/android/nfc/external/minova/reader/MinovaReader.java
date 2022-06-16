package no.entur.android.nfc.external.minova.reader;

import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class MinovaReader implements Parcelable {
    private static final String TAG = MinovaReader.class.getName();
    
    protected String name;
    public static final int STATUS_OK = 0;
    public static final int STATUS_EXCEPTION = 1;

    public static final int VERSION = 1;

    public String getName() {
        return name;
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    protected static String toHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    protected static int readInteger(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    return din.readInt();
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw new McrReaderException(e);
        }
    }

    protected static boolean readBoolean(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    return din.readBoolean();
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw new McrReaderException(e);
        }
    }

    private static IllegalArgumentException createUnexpectedVersionIllegalArgumentException(int version) {
        return new IllegalArgumentException("Unexpected version " + version);
    }

    protected static byte readByte(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    return din.readByte();
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw new McrReaderException(e);
        }
    }

    protected static String readString(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    return din.readUTF();
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            Log.d(TAG, "Problem reading string length " + response.length + ": " + toHexString(response));
            throw new McrReaderException(e);
        }
    }

    protected static byte[] readByteArray(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    int length = din.readInt();
                    byte[] array = new byte[length];
                    din.readFully(array);
                    return array;
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            Log.d(TAG, "Problem reading string length " + response.length + ": " + toHexString(response));
            throw new McrReaderException(e);
        }
    }

    protected static void readVoid(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == VERSION) {
                int status = din.readInt();

                if (status == STATUS_OK) {
                    return;
                } else {
                    throw new McrReaderException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw new McrReaderException(e);
        }
    }
}
