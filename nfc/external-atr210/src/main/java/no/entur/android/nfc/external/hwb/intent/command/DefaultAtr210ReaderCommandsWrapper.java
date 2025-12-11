package no.entur.android.nfc.external.hwb.intent.command;

import no.entur.android.nfc.external.hwb.reader.Atr210ReaderCommands;

public class DefaultAtr210ReaderCommandsWrapper extends Atr210ReaderCommandsWrapper {

    protected final Atr210ReaderCommands atr210ReaderCommands;

    public DefaultAtr210ReaderCommandsWrapper(Atr210ReaderCommands atr210ReaderCommands) {
        this.atr210ReaderCommands = atr210ReaderCommands;
    }

    @Override
    protected Atr210ReaderCommands getCommands() {
        return atr210ReaderCommands;
    }
}
