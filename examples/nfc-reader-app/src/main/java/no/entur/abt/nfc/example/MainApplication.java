package no.entur.abt.nfc.example;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import no.entur.android.nfc.external.ExternalNfcServiceAdapter;
import no.entur.android.nfc.external.acs.service.AcsUsbService;

public class MainApplication extends Application {

    public static final String PREF_KEY_EXTERNAL_NFC = "externalNfcService";

    private ExternalNfcServiceAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();

        adapter = new ExternalNfcServiceAdapter(this, AcsUsbService.class, false);

        if(isExternalNfcReader()) {
            adapter.startService(new Bundle());
        } else {
            adapter.stopService();
        }
    }

    public ExternalNfcServiceAdapter getAdapter() {
        return adapter;
    }

    public boolean isExternalNfcReader() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_EXTERNAL_NFC, false);
    }

    public void setExternalNfcReader(boolean enabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(PREF_KEY_EXTERNAL_NFC, enabled);
        if(!edit.commit()) {
            throw new IllegalStateException();
        }

        if(enabled) {
            adapter.startService(new Bundle());
        } else {
            adapter.stopService();
        }
    }

}
