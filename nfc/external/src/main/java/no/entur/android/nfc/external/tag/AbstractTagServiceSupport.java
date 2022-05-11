package no.entur.android.nfc.external.tag;

import android.content.Context;
import android.content.Intent;

import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.wrapper.INfcTag;

public class AbstractTagServiceSupport {

    public static final String ANDROID_PERMISSION_NFC = "android.permission.NFC";

    protected final Context context;
    protected final INfcTag tagService;
    protected final TagProxyStore store;

    public AbstractTagServiceSupport(Context context, INfcTag tagService, TagProxyStore store) {
        this.context = context;
        this.tagService = tagService;
        this.store = store;
    }

    public void broadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent, ANDROID_PERMISSION_NFC);
    }

}
