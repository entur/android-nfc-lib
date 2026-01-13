package no.entur.android.nfc.external.test;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

import androidx.core.util.Consumer;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.wrapper.test.MockTag;

public class MockExternalReader {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Context context;
        private String actionTagDiscovered = ExternalNfcTagCallback.ACTION_TAG_DISCOVERED;

        private String actionTagLost = ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD;

        private String actionReaderClosed = ExternalNfcReaderCallback.ACTION_READER_CLOSED;
        private String actionReaderOpened = ExternalNfcReaderCallback.ACTION_READER_OPENED;

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder withActionReaderClosed(String actionReaderClosed) {
            this.actionReaderClosed = actionReaderClosed;
            return this;
        }

        public Builder withActionReaderOpened(String actionReaderOpened) {
            this.actionReaderOpened = actionReaderOpened;
            return this;
        }

        public Builder withActionTagDiscovered(String action) {
            this.actionTagDiscovered = action;
            return this;
        }

        public Builder withActionTagLost(String action) {
            this.actionTagLost = action;
            return this;
        }

        public MockExternalReader build() {
            return new MockExternalReader(context, actionTagDiscovered, actionTagLost, actionReaderOpened, actionReaderClosed);
        }

    }

    private String actionTagDiscovered;

    private String actionTagLost;

    private String actionReaderClosed;
    private String actionReaderOpened;
    private Context context;

    public MockExternalReader(Context context, String actionTagDiscovered, String actionTagLost, String actionReaderOpened, String actionReaderClosed) {
        this.actionTagDiscovered = actionTagDiscovered;
        this.actionTagLost = actionTagLost;
        this.context = context;
        this.actionReaderOpened = actionReaderOpened;
        this.actionReaderClosed = actionReaderClosed;
    }

    public void tagEnteredField(MockTag mockTag) {
        mockTag.present();

        Intent intent = new Intent(actionTagDiscovered);

        intent.putExtra(NfcAdapter.EXTRA_TAG, mockTag);
        if (mockTag.getId() != null) {
            intent.putExtra(NfcAdapter.EXTRA_ID, mockTag.getId());
        }

        intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, mockTag.getServiceHandle());
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "mock");

        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void tagEnteredField(MockTag mockTag, Consumer<Intent> consumer) {
        mockTag.present();

        Intent intent = new Intent(actionTagDiscovered);

        intent.putExtra(NfcAdapter.EXTRA_TAG, mockTag);
        if (mockTag.getId() != null) {
            intent.putExtra(NfcAdapter.EXTRA_ID, mockTag.getId());
        }

        intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, mockTag.getServiceHandle());
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "mock");

        consumer.accept(intent);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void tagLeftField(MockTag mockTag) {
        mockTag.lost();

        Intent intent = new Intent(actionTagLost);

        intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, mockTag.getServiceHandle());
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "mock");

        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void tagLeftField(MockTag mockTag, Consumer<Intent> consumer) {
        mockTag.lost();

        Intent intent = new Intent(actionTagLost);
        intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, mockTag.getServiceHandle());
        intent.putExtra(ExternalNfcReaderCallback.EXTRAS_READER_ID, "mock");

        consumer.accept(intent);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void open() {
        Intent intent = new Intent(actionReaderOpened);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void open(Consumer<Intent> consumer) {
        Intent intent = new Intent(actionReaderOpened);
        consumer.accept(intent);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void close(Consumer<Intent> consumer) {
        Intent intent = new Intent(actionReaderClosed);
        consumer.accept(intent);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

    public void close() {
        Intent intent = new Intent(actionReaderClosed);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }
}
