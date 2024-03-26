package no.entur.android.nfc.websocket.server;

import java.util.List;

/**
 *
 * Enrich terminal, i.e. set the corresponding tags (reader model, connected card type, hardcoded values)
 *
 */

public interface CardTerminalMetadataEnricher {

    void enrich(ExtendedCardTerminal cardTerminal);

}
