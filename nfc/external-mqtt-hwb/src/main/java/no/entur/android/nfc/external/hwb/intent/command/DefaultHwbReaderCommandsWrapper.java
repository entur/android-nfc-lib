package no.entur.android.nfc.external.hwb.intent.command;

import no.entur.android.nfc.external.hwb.reader.HwbReaderCommands;

public class DefaultHwbReaderCommandsWrapper extends HwbReaderCommandsWrapper {

    protected final HwbReaderCommands hwbReaderCommands;

    public DefaultHwbReaderCommandsWrapper(HwbReaderCommands hwbReaderCommands) {
        this.hwbReaderCommands = hwbReaderCommands;
    }

    @Override
    protected HwbReaderCommands getCommands() {
        return hwbReaderCommands;
    }
}
