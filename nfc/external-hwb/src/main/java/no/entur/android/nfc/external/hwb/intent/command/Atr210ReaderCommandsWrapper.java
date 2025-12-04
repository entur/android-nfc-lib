package no.entur.android.nfc.external.hwb.intent.command;

import no.entur.android.nfc.external.hwb.reader.HwbReaderCommands;

public class Atr210ReaderCommandsWrapper extends HwbReaderCommandsWrapper {

    protected final HwbReaderCommands hwbReaderCommands;

    public Atr210ReaderCommandsWrapper(HwbReaderCommands hwbReaderCommands) {
        this.hwbReaderCommands = hwbReaderCommands;
    }

    @Override
    protected HwbReaderCommands getCommands() {
        return hwbReaderCommands;
    }
}
