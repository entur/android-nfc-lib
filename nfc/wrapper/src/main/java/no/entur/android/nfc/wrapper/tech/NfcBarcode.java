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

import no.entur.android.nfc.wrapper.Tag;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TagWrapper;

import android.os.Bundle;
import android.os.RemoteException;

/**
 * Provides access to tags containing just a barcode.
 *
 * <p>Acquire an {@link NfcBarcode} object using {@link #get}.
 *
 */
public abstract class NfcBarcode implements BasicTagTechnology {

    /** Kovio Tags */
    public static final int TYPE_KOVIO = 1;
    public static final int TYPE_UNKNOWN = -1;

    public static final String EXTRA_BARCODE_TYPE = "barcodetype";

    /**
     * Get an instance of {@link NfcBarcode} for the given tag.
     *
     * <p>Returns null if {@link NfcBarcode} was not enumerated in {@link TagImpl#getTechList}.
     *
     * <p>Does not cause any RF activity and does not block.
     *
     * @param tag an NfcBarcode compatible tag
     * @return NfcBarcode object
     */
    public static NfcBarcode get(Tag tag) {
        if(tag instanceof TagImpl) {
            TagImpl tagImpl = (TagImpl)tag;
            if (!tagImpl.hasTech(TagTechnology.NFC_BARCODE)) return null;
            try {
                return new NfcBarcodeImpl(tagImpl);
            } catch (RemoteException e) {
                return null;
            }
        } else if(tag instanceof TagWrapper) {
            TagWrapper delegate = (TagWrapper)tag;
            android.nfc.tech.NfcBarcode nfcBarcode = android.nfc.tech.NfcBarcode.get(delegate.getDelegate());
            if(nfcBarcode == null) {
                return null;
            }
            return new NfcBarcodeWrapper(nfcBarcode);
        } else {
            throw new IllegalArgumentException();
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
    public abstract int getType();

    /**
     * Returns the barcode of an NfcBarcode tag.
     *
     * @return a byte array containing the barcode
     * @see <a href="http://www.thinfilm.no/docs/thinfilm-nfc-barcode-datasheet.pdf">
     *      Thinfilm NFC Barcode tag specification (previously Kovio NFC Barcode)</a>
     * @see <a href="http://www.thinfilm.no/docs/thinfilm-nfc-barcode-data-format.pdf">
     *      Thinfilm NFC Barcode data format (previously Kovio NFC Barcode)</a>
     */
    public abstract byte[] getBarcode();
}
