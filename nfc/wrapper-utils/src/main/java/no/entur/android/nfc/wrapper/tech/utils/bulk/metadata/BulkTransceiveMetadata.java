package no.entur.android.nfc.wrapper.tech.utils.bulk.metadata;

/**
 *
 * Description of bulk metadata.
 *
 * For use with {@linkplain no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommands}.
 *
 */

public class BulkTransceiveMetadata {

    private boolean partialResponsePredicate;

    private boolean responsePredicate;

    public BulkTransceiveMetadata(boolean partialResponsePredicate, boolean responsePredicate) {
        this.partialResponsePredicate = partialResponsePredicate;
        this.responsePredicate = responsePredicate;
    }

    public boolean isPartialResponsePredicate() {
        return partialResponsePredicate;
    }

    public boolean isResponsePredicate() {
        return responsePredicate;
    }
}
