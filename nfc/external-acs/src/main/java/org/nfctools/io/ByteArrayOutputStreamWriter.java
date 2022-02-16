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
package org.nfctools.io;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import org.nfctools.utils.NfcUtils;

/**
 * Simple writer which directly writes data into an OutputStream without modification.
 * 
 */
public class ByteArrayOutputStreamWriter implements ByteArrayWriter {

	private static final String TAG = ByteArrayInputStreamReader.class.getName();

	private OutputStream outputStream;

	public ByteArrayOutputStreamWriter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		Log.d(TAG, NfcUtils.convertBinToASCII(data, offset, length));
		outputStream.write(data, offset, length);
	}
}
