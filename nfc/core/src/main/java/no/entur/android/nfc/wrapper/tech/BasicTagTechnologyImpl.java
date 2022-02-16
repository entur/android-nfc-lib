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
import android.util.Log;

import java.io.IOException;

import no.entur.android.nfc.wrapper.ErrorCodes;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;

/**
 * A base class for tag technologies that are built on top of transceive().
 */
public class BasicTagTechnologyImpl implements BasicTagTechnology {
	private static final String TAG = "NFC";
	private static final String NFC_SERVICE_DEAD_MSG = "NFC service dead";

	final TagImpl mTag;

	boolean mIsConnected;
	int mSelectedTechnology;

	BasicTagTechnologyImpl(TagImpl tag, int tech) throws RemoteException {
		mTag = tag;
		mSelectedTechnology = tech;
	}

	@Override
	public TagImpl getTag() {
		return mTag;
	}

	/** Internal helper to throw IllegalStateException if the technology isn't connected */
	void checkConnected() {
		if ((mTag.getConnectedTechnology() != mSelectedTechnology) || (mTag.getConnectedTechnology() == -1)) {
			throw new IllegalStateException("Call connect() first!");
		}
	}

	@Override
	public boolean isConnected() {
		if (!mIsConnected) {
			return false;
		}

		try {
			return mTag.getTagService().isPresent(mTag.getServiceHandle());
		} catch (RemoteException e) {

			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
			return false;
		}
	}

	@Override
	public void connect() throws IOException {
		try {
			int errorCode = mTag.getTagService().connect(mTag.getServiceHandle(), mSelectedTechnology);

			if (errorCode == ErrorCodes.SUCCESS) {
				// Store this in the tag object
				if (!mTag.setConnectedTechnology(mSelectedTechnology)) {
					Log.e(TAG, "Close other technology first!");
					throw new IOException("Only one TagTechnology can be connected at a time.");
				}
				mIsConnected = true;
			} else if (errorCode == ErrorCodes.ERROR_NOT_SUPPORTED) {
				throw new UnsupportedOperationException("Connecting to " + "this technology is not supported by the NFC " + "adapter.");
			} else {
				throw new IOException();
			}
		} catch (RemoteException e) {
			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
			throw new IOException(NFC_SERVICE_DEAD_MSG);
		}
	}

	public void reconnect() throws IOException {
		if (!mIsConnected) {
			throw new IllegalStateException("Technology not connected yet");
		}

		try {
			int errorCode = mTag.getTagService().reconnect(mTag.getServiceHandle());

			if (errorCode != ErrorCodes.SUCCESS) {
				mIsConnected = false;
				mTag.setTechnologyDisconnected();
				throw new IOException();
			}
		} catch (RemoteException e) {
			mIsConnected = false;
			mTag.setTechnologyDisconnected();
			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
			throw new IOException(NFC_SERVICE_DEAD_MSG);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			/*
			 * Note that we don't want to physically disconnect the tag, but just reconnect to it to reset its state
			 */
			mTag.getTagService().resetTimeouts();
			mTag.getTagService().reconnect(mTag.getServiceHandle());
		} catch (RemoteException e) {
			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
		} finally {
			mIsConnected = false;
			mTag.setTechnologyDisconnected();
		}
	}

	public int getMaxTransceiveLengthInternal() {
		try {
			return mTag.getTagService().getMaxTransceiveLength(mSelectedTechnology);
		} catch (RemoteException e) {
			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
			return 0;
		}
	}

	public byte[] transceive(byte[] data, boolean raw) throws IOException {
		checkConnected();

		try {
			TransceiveResult result = mTag.getTagService().transceive(mTag.getServiceHandle(), data, raw);
			if (result == null) {
				throw new IOException("transceive failed");
			} else {
				return result.getResponseOrThrow();
			}
		} catch (RemoteException e) {
			Log.e(TAG, NFC_SERVICE_DEAD_MSG, e);
			throw new IOException(NFC_SERVICE_DEAD_MSG);
		}
	}
}
