package no.entur.android.nfc.external.hwb.intent.command;

import android.os.Parcelable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.entur.android.nfc.external.hwb.reader.Atr210ReaderCommands;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public abstract class Atr210ReaderCommandsWrapper extends RemoteCommandWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Atr210ReaderCommandsWrapper.class);

    protected abstract Atr210ReaderCommands getCommands();

}
