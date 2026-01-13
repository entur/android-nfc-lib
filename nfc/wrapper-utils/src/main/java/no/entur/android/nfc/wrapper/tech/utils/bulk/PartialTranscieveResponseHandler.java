package no.entur.android.nfc.wrapper.tech.utils.bulk;

public class PartialTranscieveResponseHandler {

    protected final String id;

    protected final PartialTranscieveResponsePredicate predicate;
    protected final PartialTranscieveResponseReaderFactory factory;

    public PartialTranscieveResponseHandler(String id, PartialTranscieveResponsePredicate predicate, PartialTranscieveResponseReaderFactory factory) {
        this.id = id;
        this.predicate = predicate;
        this.factory = factory;
    }

    public PartialTranscieveResponsePredicate getPredicate() {
        return predicate;
    }

    public PartialTranscieveResponseReaderFactory getFactory() {
        return factory;
    }

    public String getId() {
        return id;
    }
}
