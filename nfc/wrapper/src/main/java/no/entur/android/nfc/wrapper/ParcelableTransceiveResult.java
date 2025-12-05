/*
 * Copyright (C) 2011, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.entur.android.nfc.wrapper;

import android.nfc.TagLostException;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

/**
 * Class used to pipe extended transceive result from the NFC service.
 *
 */
public class ParcelableTransceiveResult implements Parcelable {

	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILURE = 1;
	public static final int RESULT_TAGLOST = 2;
	public static final int RESULT_EXCEEDED_LENGTH = 3;

	final int mResult;
	final Parcelable mResponseData;

	public ParcelableTransceiveResult(final int result, final Parcelable data) {
		mResult = result;
		mResponseData = data;
	}

	public Parcelable getResponseOrThrow() throws IOException {
		switch (mResult) {
		case RESULT_SUCCESS:
			return mResponseData;
		case RESULT_TAGLOST:
			throw new TagLostException("Tag was lost.");
		case RESULT_EXCEEDED_LENGTH:
			throw new IOException("Transceive length exceeds supported maximum");
		default:
			throw new IOException("Transceive failed");
		}
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mResult);
		if (mResult == RESULT_SUCCESS) {
			dest.writeParcelable(mResponseData, 0);
		}
	}

	public static final Creator<ParcelableTransceiveResult> CREATOR = new Creator<ParcelableTransceiveResult>() {
		@Override
		public ParcelableTransceiveResult createFromParcel(Parcel in) {
			int result = in.readInt();
			Parcelable responseData;

			if (result == RESULT_SUCCESS) {
                responseData = in.readParcelable(this.getClass().getClassLoader());
			} else {
				responseData = null;
			}
			return new ParcelableTransceiveResult(result, responseData);
		}

		@Override
		public ParcelableTransceiveResult[] newArray(int size) {
			return new ParcelableTransceiveResult[size];
		}
	};

}
