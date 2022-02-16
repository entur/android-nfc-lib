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

package no.entur.android.nfc.wrapper;

import android.nfc.NdefMessage;
import no.entur.android.nfc.wrapper.TagImpl;
import no.entur.android.nfc.wrapper.TransceiveResult;

/**
 * @hide
 */
interface INfcTag
{
    int connect(int nativeHandle, int technology);
    int reconnect(int nativeHandle);
    int[] getTechList(int nativeHandle);
    boolean isNdef(int nativeHandle);
    boolean isPresent(int nativeHandle);
    TransceiveResult transceive(int nativeHandle, in byte[] data, boolean raw);

    TagImpl rediscover(int nativehandle);

    int setTimeout(int technology, int timeout);
    int getTimeout(int technology);
    void resetTimeouts();
    boolean canMakeReadOnly(int ndefType);
    int getMaxTransceiveLength(int technology);
    boolean getExtendedLengthApdusSupported();
}
