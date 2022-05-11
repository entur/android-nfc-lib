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

import android.os.RemoteException;

import java.io.IOException;

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TagWrapper;

/**
 * Provides access to NFC-V (ISO 15693) properties and I/O operations on a {@link TagImpl}.
 *
 * <p>
 * Acquire a {@link NfcV} object using {@link #get}.
 * <p>
 * The primary NFC-V I/O operation is {@link #transceive}. Applications must implement their own protocol stack on top of {@link #transceive}.
 *
 * <p class="note">
 * <strong>Note:</strong> Methods that perform I/O operations require the {@link android.Manifest.permission#NFC} permission.
 */
public abstract class NfcV implements BasicTagTechnology {
	/** @hide */
	public static final String EXTRA_RESP_FLAGS = "respflags";

	/** @hide */
	public static final String EXTRA_DSFID = "dsfid";

	private byte mRespFlags;
	private byte mDsfId;

	/**
	 * Get an instance of {@link NfcV} for the given tag.
	 * <p>
	 * Returns null if {@link NfcV} was not enumerated in {@link TagImpl#getTechList}. This indicates the tag does not support NFC-V.
	 * <p>
	 * Does not cause any RF activity and does not block.
	 *
	 * @param tag an NFC-V compatible tag
	 * @return NFC-V object
	 */
	public static NfcV get(Tag tag) {
		if (tag instanceof TagImpl) {
			TagImpl tagImpl = (TagImpl) tag;
			if (!tagImpl.hasTech(TagTechnology.NFC_V))
				return null;
			try {
				return new NfcVImpl(tagImpl);
			} catch (RemoteException e) {
				return null;
			}
		} else if (tag instanceof TagWrapper) {
			TagWrapper delegate = (TagWrapper) tag;
			android.nfc.tech.NfcV nfcV = android.nfc.tech.NfcV.get(delegate.getDelegate());
			if (nfcV == null) {
				return null;
			}
			return new NfcVWrapper(nfcV);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Return the Response Flag bytes from tag discovery.
	 *
	 * <p>
	 * Does not cause any RF activity and does not block.
	 *
	 * @return Response Flag bytes
	 */
	public byte getResponseFlags() {
		return mRespFlags;
	}

	/**
	 * Return the DSF ID bytes from tag discovery.
	 *
	 * <p>
	 * Does not cause any RF activity and does not block.
	 *
	 * @return DSF ID bytes
	 */
	public byte getDsfId() {
		return mDsfId;
	}

	/**
	 * Send raw NFC-V commands to the tag and receive the response.
	 *
	 * <p>
	 * Applications must not append the CRC to the payload, it will be automatically calculated. The application does provide FLAGS, CMD and PARAMETER bytes.
	 *
	 * <p>
	 * Use {@link #getMaxTransceiveLength} to retrieve the maximum amount of bytes that can be sent with {@link #transceive}.
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
	public abstract byte[] transceive(byte[] data) throws IOException;

	/**
	 * Return the maximum number of bytes that can be sent with {@link #transceive}.
	 * 
	 * @return the maximum number of bytes that can be sent with {@link #transceive}.
	 */
	public abstract int getMaxTransceiveLength();
}
