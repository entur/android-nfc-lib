package no.entur.android.nfc.external.minova.service;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class MinovaTransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {

    @Override
    public TransceiveResult mapException(Exception e) {
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
