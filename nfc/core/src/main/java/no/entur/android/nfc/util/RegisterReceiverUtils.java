package no.entur.android.nfc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;

public class RegisterReceiverUtils {

    private RegisterReceiverUtils() {
    }

    public static void registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
        } else {
            context.registerReceiver(receiver, filter, broadcastPermission, scheduler);
        }
    }

    public static void registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, flags);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(receiver, filter, flags);
        } else {
            context.registerReceiver(receiver, filter);
        }
    }
}
