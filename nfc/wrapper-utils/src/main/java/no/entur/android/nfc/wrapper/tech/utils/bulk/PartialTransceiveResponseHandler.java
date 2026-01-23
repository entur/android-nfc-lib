package no.entur.android.nfc.wrapper.tech.utils.bulk;

public class PartialTransceiveResponseHandler {

    protected final String id;

    protected final PartialTransceiveResponsePredicate predicate;
    protected final PartialTransceiveResponseReaderFactory factory;

    public PartialTransceiveResponseHandler(String id, PartialTransceiveResponsePredicate predicate, PartialTransceiveResponseReaderFactory factory) {
        this.id = id;
        this.predicate = predicate;
        this.factory = factory;
    }

    public PartialTransceiveResponsePredicate getPredicate() {
        return predicate;
    }

    public PartialTransceiveResponseReaderFactory getFactory() {
        return factory;
    }

    public String getId() {
        return id;
    }
}
