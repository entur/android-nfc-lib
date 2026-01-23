package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

/**
 *
 * Description of bulk metadata.
 *
 * For use with {@linkplain no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommands}.
 *
 */

public class BulkTransceiveMetadata {

    private boolean partialTransceiveResponsePredicate;

    private boolean transceiveResponsePredicate;

    public BulkTransceiveMetadata(boolean partialTransceiveResponsePredicate, boolean transceiveResponsePredicate) {
        this.partialTransceiveResponsePredicate = partialTransceiveResponsePredicate;
        this.transceiveResponsePredicate = transceiveResponsePredicate;
    }

    public boolean isPartialTransceiveResponsePredicate() {
        return partialTransceiveResponsePredicate;
    }

    public boolean isTransceiveResponsePredicate() {
        return transceiveResponsePredicate;
    }
}
