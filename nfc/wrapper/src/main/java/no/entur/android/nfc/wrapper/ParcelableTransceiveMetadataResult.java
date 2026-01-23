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
public class ParcelableTransceiveMetadataResult implements Parcelable {

	final Parcelable mResponseData;

    public ParcelableTransceiveMetadataResult() {
        mResponseData = null;
    }

    public ParcelableTransceiveMetadataResult(final Parcelable data) {
		mResponseData = data;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mResponseData != null ? 1 : 0);
		if (mResponseData != null) {
			dest.writeParcelable(mResponseData, 0);
		}
	}

	public static final Creator<ParcelableTransceiveMetadataResult> CREATOR = new Creator<ParcelableTransceiveMetadataResult>() {
		@Override
		public ParcelableTransceiveMetadataResult createFromParcel(Parcel in) {
			int present = in.readInt();
			Parcelable responseData;

			if (present == 1) {
                responseData = in.readParcelable(this.getClass().getClassLoader());
			} else {
				responseData = null;
			}
			return new ParcelableTransceiveMetadataResult(responseData);
		}

		@Override
		public ParcelableTransceiveMetadataResult[] newArray(int size) {
			return new ParcelableTransceiveMetadataResult[size];
		}
	};

}
