package no.entur.android.nfc.external.minova.reader;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import no.entur.android.nfc.external.ExternalNfcTagCallback;
import no.entur.android.nfc.external.minova.service.MinovaService;
import no.entur.android.nfc.external.tag.AbstractReaderIsoDepWrapper;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaIsoDepWrapper extends AbstractReaderIsoDepWrapper {

    private final MinovaCommands commands;
    private final Context context;

    public MinovaIsoDepWrapper(CommandInputOutputThread<String, String> reader, Context context) {
        super(-1);
        this.commands = new MinovaCommands(reader);
        this.context = context;
    }

    @Override
    public byte[] transceive(byte[] data) throws Exception {
        try {
            return commands.sendAdpu(data);
        } catch(McrReaderException e) {
            // there is no "tag lost" even, so make sure to loose the tag as soon as possible if some command does not respond
            tagProxy.close();

            sendTagLeftFieldBroadcast();

            throw e;
        }
    }

    @Override
    public byte[] transceiveRaw(byte[] data) throws Exception {
        try {
            return commands.sendAdpu(data);
        } catch(McrReaderException e) {
            // there is no "tag lost" even, so make sure to loose the tag as soon as possible if some command does not respond
            tagProxy.close();

            sendTagLeftFieldBroadcast();

            throw e;
        }
    }

    private void sendTagLeftFieldBroadcast() {
        Intent intent = new Intent();
        intent.setAction(ExternalNfcTagCallback.ACTION_TAG_LEFT_FIELD);
        intent.putExtra(MinovaService.EXTRA_TAG_LEFT_FIELD_REASON, MinovaService.EXTRA_TAG_LEFT_FIELD_REASON_NEW_TAG);
        intent.putExtra(MinovaService.EXTRA_TAG_LEFT_FIELD_SERVICE_HANDLE, tagProxy.getHandle());

        intent.putExtra(ExternalNfcTagCallback.EXTRAS_TAG_HANDLE, tagProxy.getHandle());

        byte[] uid = tagProxy.getUid();

        if(uid != null) {
            intent.putExtra(NfcAdapter.EXTRA_TAG, uid);
        }

        context.sendBroadcast(intent, "android.permission.NFC");
    }

    @Override
    public Parcelable transceive(Parcelable parcelable) throws Exception {
        throw new RuntimeException("Not implemented");
    }

}
