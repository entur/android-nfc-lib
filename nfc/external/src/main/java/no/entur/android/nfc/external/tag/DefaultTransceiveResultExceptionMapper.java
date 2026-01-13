package no.entur.android.nfc.external.tag;

import no.entur.android.nfc.wrapper.TransceiveResult;

public class DefaultTransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {
    @Override
    public TransceiveResult mapException(Exception e) {
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
