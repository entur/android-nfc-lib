package no.entur.android.nfc.external.hwb.intent;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class HwbTransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {

    @Override
    public TransceiveResult mapException(Exception e) {
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
