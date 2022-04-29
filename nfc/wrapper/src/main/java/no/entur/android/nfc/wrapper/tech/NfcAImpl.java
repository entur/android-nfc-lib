/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.entur.android.nfc.wrapper.tech;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;

/**
 * Provides access to NFC-A (ISO 14443-3A) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>
 * Acquire a {@link NfcAImpl} object using {@link #get}.
 * <p>
 * The primary NFC-A I/O operation is {@link #transceive}. Applications must implement their own protocol stack on top of {@link #transceive}.
 *
 * <p class="note">
 * <strong>Note:</strong> Methods that perform I/O operations require the {@link android.Manifest.permission#NFC} permission.
 */
public final class NfcAImpl extends NfcA {
	private static final String TAG = "NFC";

	private short mSak;
	private byte[] mAtqa;

	private BasicTagTechnologyImpl delegate;

	public NfcAImpl(TagImpl tag) throws RemoteException {
		this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.NFC_A);

		Bundle extras = tag.getTechExtras(TagTechnology.NFC_A);
		mSak = extras.getShort(EXTRA_SAK);
		mAtqa = extras.getByteArray(EXTRA_ATQA);
	}

	/**
	 * Return the ATQA/SENS_RES bytes from tag discovery.
	 *
	 * <p>
	 * Does not cause any RF activity and does not block.
	 *
	 * @return ATQA/SENS_RES bytes
	 */
	@Override
	public byte[] getAtqa() {
		return mAtqa;
	}

	/**
	 * Return the SAK/SEL_RES bytes from tag discovery.
	 *
	 * <p>
	 * Does not cause any RF activity and does not block.
	 *
	 * @return SAK bytes
	 */
	@Override
	public short getSak() {
		return mSak;
	}

	/**
	 * Send raw NFC-A commands to the tag and receive the response.
	 *
	 * <p>
	 * Applications must not append the EoD (CRC) to the payload, it will be automatically calculated.
	 * <p>
	 * Applications must only send commands that are complete bytes, for example a SENS_REQ is not possible (these are used to manage tag polling and
	 * initialization).
	 *
	 * <p>
	 * Use {@link #getMaxTransceiveLength} to retrieve the maximum number of bytes that can be sent with {@link #transceive}.
	 *
	 * <p>
	 * This is an I/O operation and will block until complete. It must not be called from the main application thread. A blocked call will be canceled with
	 * {@link IOException} if {@link #close} is called from another thread.
	 *
	 * <p class="note">
	 * Requires the {@link android.Manifest.permission#NFC} permission.
	 *
	 * @param data bytes to send
	 * @return bytes received in response
	 * @throws android.nfc.TagLostException if the tag leaves the field
	 * @throws IOException                  if there is an I/O failure, or this operation is canceled
	 */
	@Override
	public byte[] transceive(byte[] data) throws IOException {
		return delegate.transceive(data, true);
	}

	/**
	 * Return the maximum number of bytes that can be sent with {@link #transceive}.
	 * 
	 * @return the maximum number of bytes that can be sent with {@link #transceive}.
	 */
	@Override
	public int getMaxTransceiveLength() {
		return delegate.getMaxTransceiveLengthInternal();
	}

	/**
	 * Set the {@link #transceive} timeout in milliseconds.
	 *
	 * <p>
	 * The timeout only applies to {@link #transceive} on this object, and is reset to a default value when {@link #close} is called.
	 *
	 * <p>
	 * Setting a longer timeout may be useful when performing transactions that require a long processing time on the tag such as key generation.
	 *
	 * <p class="note">
	 * Requires the {@link android.Manifest.permission#NFC} permission.
	 *
	 * @param timeout timeout value in milliseconds
	 */
	@Override
	public void setTimeout(int timeout) {
		try {
			int err = delegate.getTag().getTagService().setTimeout(TagTechnology.NFC_A, timeout);
			if (err != ErrorCodes.SUCCESS) {
				throw new IllegalArgumentException("The supplied timeout is not valid");
			}
		} catch (RemoteException e) {
			Log.e(TAG, "NFC service dead", e);
		}
	}

	/**
	 * Get the current {@link #transceive} timeout in milliseconds.
	 *
	 * <p class="note">
	 * Requires the {@link android.Manifest.permission#NFC} permission.
	 *
	 * @return timeout value in milliseconds
	 */
	@Override
	public int getTimeout() {
		try {
			return delegate.getTag().getTagService().getTimeout(TagTechnology.NFC_A);
		} catch (RemoteException e) {
			Log.e(TAG, "NFC service dead", e);
			return 0;
		}
	}

	@Override
	public Tag getTag() {
		return delegate.getTag();
	}

	@Override
	public void connect() throws IOException {
		delegate.connect();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}

}
