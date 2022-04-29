/*
 * Copyright (C) 2012 The Android Open Source Project
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

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;

import java.io.IOException;

/**
 * Provides access to tags containing just a barcode.
 *
 * <p>Acquire an {@link NfcBarcodeImpl} object using {@link #get}.
 *
 */
public final class NfcBarcodeImpl extends NfcBarcode {

    /** Kovio Tags */
    public static final int TYPE_KOVIO = 1;
    public static final int TYPE_UNKNOWN = -1;

    public static final String EXTRA_BARCODE_TYPE = "barcodetype";

    private int mType;

    /**
     * Internal constructor, to be used by NfcAdapter
     */

    private BasicTagTechnologyImpl delegate;

    public NfcBarcodeImpl(TagImpl tag) throws RemoteException {
        this.delegate = new BasicTagTechnologyImpl(tag, TagTechnology.NFC_BARCODE);

        Bundle extras = tag.getTechExtras(TagTechnology.NFC_BARCODE);
        if (extras != null) {
            mType = extras.getInt(EXTRA_BARCODE_TYPE);
        } else {
            throw new NullPointerException("NfcBarcode tech extras are null.");
        }
    }

    /**
     * Returns the NFC Barcode tag type.
     *
     * <p>Currently only one of {@link #TYPE_KOVIO} or {@link #TYPE_UNKNOWN}.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @return the NFC Barcode tag type
     */
    public int getType() {
        return mType;
    }

    /**
     * Returns the barcode of an NfcBarcode tag.
     *
     * @return a byte array containing the barcode
     * @see <a href="http://www.thinfilm.no/docs/thinfilm-nfc-barcode-datasheet.pdf">
     *      Thinfilm NFC Barcode tag specification (previously Kovio NFC Barcode)</a>
     * @see <a href="http://www.thinfilm.no/docs/thinfilm-nfc-barcode-data-format.pdf">
     *      Thinfilm NFC Barcode data format (previously Kovio NFC Barcode)</a>
     */
    public byte[] getBarcode() {
        switch (mType) {
            case TYPE_KOVIO:
                // For Kovio tags the barcode matches the ID
                return delegate.getTag().getId();
            default:
                return null;
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
