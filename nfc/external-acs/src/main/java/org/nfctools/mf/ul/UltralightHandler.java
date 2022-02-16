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
package org.nfctools.mf.ul;

import java.util.Arrays;

import org.nfctools.mf.block.MfBlock;

public class UltralightHandler {

	private static final byte[] NF203BlankPage3 = new byte[] { (byte) 0xE1, 0x10, 0x12, 0x00 };
	private static final byte[] NF203BlankPage4 = new byte[] { 0x01, 0x03, (byte) 0xA0, 0x10 };
	private static final byte[] NF203BlankPage5 = new byte[] { 0x44, 0x03, 0, (byte) 0xFE };

	private static final String TAG = UltralightHandler.class.getName();

	public static boolean isBlank(MfBlock[] blocks) {
		if (blocks.length < 5) {
			throw new IllegalArgumentException("need at least 5 blocks");
		}
		if (blocks[0].getData()[0] == (byte) 0x04) // manufacturer ID
		{
			if (isStaticallyUnlocked(blocks[2].getData()) && isOTPClear(blocks[3].getData())) {
				return isUltralight(blocks[4].getData()) || isUltralightC(blocks[4].getData()) || isNTAG203F(blocks[4].getData());
			}
		}
		return false;
	}

	public static boolean isBlankNTAG203(MfBlock[] blocks) {
		return Arrays.equals(blocks[3].getData(), NF203BlankPage3) && Arrays.equals(blocks[4].getData(), NF203BlankPage4);
	}

	private static boolean isOTPClear(byte[] data) {
		for (int x = 0; x < 4; x++) {
			if (data[x] != 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean isStaticallyUnlocked(byte[] block) {
		return block[2] == 0 && block[3] == 0;
	}

	public static boolean isNTAG203F(byte[] block) {
		return matchesVersion(block, 0x44, 0x03);
	}

	public static boolean isUltralight(byte[] block) {
		return matchesVersion(block, 0xff, 0xff);
	}

	public static boolean isUltralightC(byte[] block) {
		return matchesVersion(block, 0x02, 0x00);
	}

	private static boolean matchesVersion(byte[] block, int major, int minor) {
		return block[0] == (byte) major && block[1] == (byte) minor;
	}

	public static boolean isFormatted(MfBlock[] blocks) {
		return isCapabilityContainerValid(blocks[3].getData());
	}

	private static boolean isCapabilityContainerValid(byte[] block) {
		// Log.d(TAG, "isCapabilityContainerValid: " + Utils.toHexString(block));

		return (block[0] == (byte) 0xE1 && block[1] == (byte) 0x10 && block[2] >= (byte) 0x06)
				|| (block[0] == (byte) 0xE1 && block[1] == (byte) 0x11 && block[2] >= (byte) 0x06);
	}

	public static byte[] extractId(MfBlock[] idBlocks) {
		byte[] id = new byte[7];
		System.arraycopy(idBlocks[0].getData(), 0, id, 0, 3);
		System.arraycopy(idBlocks[1].getData(), 0, id, 3, 4);
		return id;
	}
}
