package no.entur.android.nfc.external.service;

import android.content.Intent;

public interface ExternalNfcReaderStatusListener<T> {

    void onReaderClosed(int readerStatus, String statusMessage);

    void onReaderOpen(T reader, int readerStatusOk);

    void onReaderStatusIntent(Intent intent);
}
