package no.entur.android.nfc.external.remote;

import android.os.Parcelable;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import no.entur.android.nfc.external.service.AbstractService;

public abstract class RemoteCommandReader implements Parcelable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCommandReader.class);

    protected String name;

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

    protected int readInteger(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    return din.readInt();
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw createRemoteCommandException(e);
        }
    }

    protected int[] readIntegers(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    int count = din.readInt();
                    int[] integers = new int[count];
                    for(int i = 0; i < count; i++) {
                        integers[i] = din.readInt();
                    }
                    return integers;
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw createRemoteCommandException(e);
        }
    }


    protected abstract RemoteCommandException createRemoteCommandException(Exception e);
    
    protected abstract RemoteCommandException createRemoteCommandException(String string);

    protected boolean readBoolean(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    return din.readBoolean();
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw createRemoteCommandException(e);
        }
    }

    private static IllegalArgumentException createUnexpectedVersionIllegalArgumentException(int version) {
        return new IllegalArgumentException("Unexpected version " + version);
    }

    protected byte readByte(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    return din.readByte();
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            throw createRemoteCommandException(e);
        }
    }

    protected String readString(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    return din.readUTF();
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            LOGGER.debug("Problem reading string length " + response.length + ": " + toHexString(response));
            throw createRemoteCommandException(e);
        }
    }

    protected byte[] readByteArray(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    int length = din.readInt();
                    byte[] array = new byte[length];
                    din.readFully(array);
                    return array;
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            LOGGER.debug("Problem reading string length " + response.length + ": " + toHexString(response));
            throw createRemoteCommandException(e);
        }
    }

    protected void readVoid(byte[] response) {
        try {
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));

            int version = din.readInt();
            if (version == RemoteCommandWriter.VERSION) {
                int status = din.readInt();

                if (status == RemoteCommandWriter.STATUS_OK) {
                    return;
                } else {
                    throw createRemoteCommandException(din.readUTF());
                }
            } else {
                throw createUnexpectedVersionIllegalArgumentException(version);
            }
        } catch (IOException e) {
            LOGGER.debug("Problem reading void " + response.length + ": " + toHexString(response));
            throw createRemoteCommandException(e);
        }
    }
}
