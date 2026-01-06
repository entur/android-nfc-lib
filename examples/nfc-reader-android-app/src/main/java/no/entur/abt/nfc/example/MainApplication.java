package no.entur.abt.nfc.example;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import no.entur.android.nfc.external.ExternalNfcServiceAdapter;
import no.entur.android.nfc.external.acs.service.AcsUsbService;
import no.entur.android.nfc.external.hid.HidMqttService;

public class MainApplication extends Application {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    public static final String PREF_KEY_EXTERNAL_NFC = "externalNfcService";

    public static final String PREF_KEY_EXTERNAL_HID_NFC = "externalHidNfcService";

    static {
        configureLogbackDirectly();
    }

    private static void configureLogbackDirectly() {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(logcatAppender);
    }

    private ExternalNfcServiceAdapter usbAdapter;
    private ExternalNfcServiceAdapter hidAdapter;

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void onCreate() {
        super.onCreate();

        usbAdapter = new ExternalNfcServiceAdapter(this, AcsUsbService.class, false);
        hidAdapter = new ExternalNfcServiceAdapter(this, HidMqttService.class, false);

        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        threadPoolExecutor.setRejectedExecutionHandler((r, executor) -> {
            LOGGER.error( "Rejected execution for " + r + " " + executor);
        });

        androidx.preference.PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
    }

    public ExternalNfcServiceAdapter getUsbAdapter() {
        return usbAdapter;
    }

    public ExternalNfcServiceAdapter getHidAdapter() {
        return hidAdapter;
    }

    public boolean isExternalNfcUsbReader() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_EXTERNAL_NFC, false);
    }

    public boolean isExternalNfcHidReader() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_EXTERNAL_HID_NFC, false);
    }

    public void setExternalNfcReader(boolean enabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(PREF_KEY_EXTERNAL_NFC, enabled);
        if(!edit.commit()) {
            throw new IllegalStateException();
        }

        if(enabled) {
            usbAdapter.startService(new Bundle());
        } else {
            usbAdapter.stopService();
        }
    }


    public void setExternalHidNfcReader(boolean enabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(PREF_KEY_EXTERNAL_HID_NFC, enabled);
        if(!edit.commit()) {
            throw new IllegalStateException();
        }

        if(enabled) {
            Bundle bundle = new Bundle();
            String host = preferences.getString(SettingsActivity.PREF_KEY_MQTT_HOST, null);

            bundle.putString(HidMqttService.MQTT_CLIENT_HOST, host);

            hidAdapter.startService(bundle);
        } else {
            hidAdapter.stopService();
        }
    }


    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}
