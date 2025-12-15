package no.entur.android.nfc.external.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class RemoteCommandWriter {

	private static final String TAG = RemoteCommandWriter.class.getName();

	// http://stackoverflow.com/questions/15604145/recommended-approach-for-handling-errors-across-process-using-aidl-android

	public static final int STATUS_OK = 0;
	public static final int STATUS_EXCEPTION = 1;

	public static final int VERSION = 1;

	protected byte[] returnValue(Integer integer, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (integer != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeInt(integer);
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(String string, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (string != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeUTF(string);
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(Boolean value, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (value != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeBoolean(value);
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(byte[] value, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (value != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeInt(value.length);
				dout.write(value);
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			// Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(Byte picc, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (picc != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeByte(picc & 0xFF); // strictly speaking not necessary with an and
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			// Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (exception == null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			// Log.d(TAG, "Send response length " + response.length + ":" + ACRCommands.toHexString(response));

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] returnValue(int[] integers, Exception exception) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			dout.writeInt(RemoteCommandWriter.VERSION);

			if (integers != null) {
				dout.writeInt(RemoteCommandWriter.STATUS_OK);
				dout.writeInt(integers.length);
				for(int i = 0; i < integers.length; i++) {
					dout.writeInt(integers[i]);
				}
			} else {
				dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
				dout.writeUTF(exception.toString());
			}
			byte[] response = out.toByteArray();

			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    protected byte[] returnValue(List<String> results, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(RemoteCommandWriter.VERSION);

            if (results != null) {
                dout.writeInt(RemoteCommandWriter.STATUS_OK);
                dout.writeInt(results.size());
                for (String result : results) {
                    dout.writeUTF(result);
                }
            } else {
                dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    protected byte[] returnValue(Parcelable parcelable, Exception exception) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(RemoteCommandWriter.VERSION);

            if (parcelable != null) {
                dout.writeInt(RemoteCommandWriter.STATUS_OK);

                Parcel parcel = Parcel.obtain();
                parcelable.writeToParcel(parcel, 0);
                byte[] marshall = parcel.marshall();
                parcel.recycle();

                dout.writeInt(marshall.length);
                dout.write(marshall);
            } else {
                dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
                dout.writeUTF(exception.toString());
            }
            byte[] response = out.toByteArray();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] noReaderException() {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(RemoteCommandWriter.VERSION);
            dout.writeInt(RemoteCommandWriter.STATUS_EXCEPTION);
            dout.writeUTF("Reader not connected");

            byte[] response = out.toByteArray();

            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readParcelable(byte[] response, Parcelable.Creator<T> creator) {
        return RemoteCommandReader.unmarshall(response, creator);
    }


}
