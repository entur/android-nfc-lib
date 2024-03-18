package no.entur.android.nfc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

public class RegisterReceiverUtils {

    private RegisterReceiverUtils() {
    }

    public static void registerReceiverNotExported(Context context, BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, broadcastPermission, scheduler, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter, broadcastPermission, scheduler);
        }
    }

    public static void registerReceiverNotExported(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver, filter);
        }
    }
}
