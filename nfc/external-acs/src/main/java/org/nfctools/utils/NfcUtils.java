/**
 * Copyright 2011-2012 Adrian Stabiszewski, as@org.nfctools.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nfctools.utils;

public class NfcUtils {

	public static String convertBinToASCII(byte[] bin) {
		return convertBinToASCII(bin, 0, bin.length);
	}

	public static String convertBinToASCII(byte[] bin, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		for (int x = offset; x < offset + length; x++) {
			String s = Integer.toHexString(bin[x]);

			if (s.length() == 1) {
				sb.append('0');
			} else {
				s = s.substring(s.length() - 2);
			}
			sb.append(s);
		}
		return sb.toString().toUpperCase();
	}

	public static byte[] intTo4Bytes(int i) {
		return new byte[] { (byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255) };
	}

	public static int bytesToInt(byte[] b, int offset) {
		return (b[offset] & 255) << 24 | (b[offset + 1] & 255) << 16 | (b[offset + 2] & 255) << 8 | (b[offset + 3] & 255);
	}

}
