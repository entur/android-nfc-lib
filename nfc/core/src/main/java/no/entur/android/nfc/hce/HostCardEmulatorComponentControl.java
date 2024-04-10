package no.entur.android.nfc.hce;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 *
 * Utility for enabling/disabling HCE component.<br><br>
 *
 * Add android:enabled="false" attribute to the service in the Android mainfest if disabled by default.
 *
 * @see <a href="https://stackoverflow.com/questions/38741564/manually-disable-and-enable-android-hostapduservice">manually-disable-and-enable-android-hostapduservice</a>
 * @see <a href="https://stackoverflow.com/questions/25487365/can-i-prevent-host-card-emulation-service-from-being-triggered-by-select-aid">can-i-prevent-host-card-emulation-service-from-being-triggered-by-select-aid</a>
 *
 */

public class HostCardEmulatorComponentControl {

    private final Context context;
    private final Class c;

    public HostCardEmulatorComponentControl(Context context, Class c) {
        this.context = context;
        this.c = c;
    }

    public void setEnabled(boolean enabled) {
        if(enabled) {
            enable();
        } else {
            disable();
        }
    }
    public void enable() {
        set(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    protected void set(int value) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, c.getName()),
                value,
                PackageManager.DONT_KILL_APP
        );
    }

    public void disable() {
        set(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    public boolean isEnabled() {
        PackageManager pm = context.getPackageManager();
        return pm.getComponentEnabledSetting(new ComponentName(context, c.getName())) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

}
