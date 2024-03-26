package no.entur.android.nfc.websocket.android;

import android.content.Context;
import android.content.Intent;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.websocket.messages.reader.ReaderClient;

public class ReaderClientListener implements ReaderClient.Listener {

    private final Context context;

    public ReaderClientListener(Context context) {
        this.context = context;
    }

    @Override
    public void onReaderDisconnected() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
        context.sendBroadcast(intent, WebSocketNfcService.ANDROID_PERMISSION_NFC);
    }


}
