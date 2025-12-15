package no.entur.android.nfc.external.atr210.schema;

import androidx.annotation.NonNull;

public class ClientIdToProviderIdConverter {

    public static String convert(String clientId) {
        String connectionType = clientId.substring(0, 2);

        int index = findUniquifierIndex(clientId);
        if(index == -1) {
            throw new IllegalArgumentException(clientId);
        }

        String entityIdentifier = clientId.substring(2, index);
        String uniquifier = clientId.substring(index);

        // itxpt.inventory/providers/itxpt.ticketreader.ATR210EH.1234567890
        return "itxpt.inventory/providers/itxpt.ticketreader." + entityIdentifier + "." + uniquifier;
    }

    @NonNull
    private static int findUniquifierIndex(String clientId) {
        for(int i = clientId.length() - 1; i >= 0; i--) {
            if(!Character.isDigit(clientId.charAt(i))) {
                return i + 1;
            }
        }
        return -1;
    }
}
