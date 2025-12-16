package no.entur.android.nfc.external.atr210.card;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class Atr210TransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {

    @Override
    public TransceiveResult mapException(Exception e) {
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
