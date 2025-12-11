package hwb.utilities.mqtt3.client;

public class ByteArrayHexStringConverter {

	/**
	 * Utility metoh to convert a byte array to a hexadecimal string.
	 *
	 * @param data Bytes to convert
	 * @return String, containing hexadecimal representation.
	 */
	public static String toHexString(byte[] data) {
		return toHexString(data, "%02X");
	}

	/**
	 * Utility method to convert a byte array to a hexadecimal string.
	 *
	 * @param data   Bytes to convert
	 * @param offset offset
	 * @param length length
	 *
	 * @return String, containing hexadecimal representation.
	 */
	public static String toHexString(byte[] data, int offset, int length) {
		return toHexString(data, offset, length, "%02X");
	}

	public static String toHexString(byte[] data, int offset, int length, String format) {
		StringBuilder sb = new StringBuilder();
		if (data != null) {
			for (int i = offset; i < offset + length; i++) {
				sb.append(String.format(format, data[i]));
			}
		}
		return sb.toString();
	}

	public static String toHexString(byte[] data, String format) {
		StringBuilder sb = new StringBuilder();
		if (data != null) {
			for (byte b : data) {
				sb.append(String.format(format, b));
			}
		}
		return sb.toString();
	}

	public static String byteToHexString(byte b) {
		return String.format("%02X", b);
	}

	public static String byteArrayToSpacedHexString(byte[] data) {
		String s = toHexString(data, "%02X ");
		if (s.length() > 0) {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

}
