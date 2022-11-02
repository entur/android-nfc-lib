package no.entur.abt.nfc.example;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import no.entur.android.nfc.external.ExternalNfcServiceAdapter;
import no.entur.android.nfc.external.acs.service.AcsUsbService;

public class MainApplication extends Application {

    public static final String PREF_KEY_EXTERNAL_NFC = "externalNfcService";

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
