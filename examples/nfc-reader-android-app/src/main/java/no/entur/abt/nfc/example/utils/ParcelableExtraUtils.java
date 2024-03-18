package no.entur.abt.nfc.example.utils;

import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;

public class ParcelableExtraUtils {
    private ParcelableExtraUtils() {

    }

    public static <T extends Parcelable> T getParcelableExtra(Intent intent, String name, Class<T> parcelableClass) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return intent.getParcelableExtra(name, parcelableClass);
            } else {
                return intent.getParcelableExtra(name);
            }
    }

}
