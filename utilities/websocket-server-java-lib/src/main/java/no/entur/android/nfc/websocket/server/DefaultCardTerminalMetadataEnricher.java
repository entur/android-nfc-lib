package no.entur.android.nfc.websocket.server;

import java.util.Collections;
import java.util.List;

public class DefaultCardTerminalMetadataEnricher implements CardTerminalMetadataEnricher {
    @Override
    public void enrich(ExtendedCardTerminal cardTerminal) {
        cardTerminal.setTags(Collections.emptyList());
    }
}
