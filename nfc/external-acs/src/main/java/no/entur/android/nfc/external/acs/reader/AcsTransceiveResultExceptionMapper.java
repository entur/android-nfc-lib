package no.entur.android.nfc.external.acs.reader;

import com.acs.smartcard.UnresponsiveCardException;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class AcsTransceiveResultExceptionMapper implements TransceiveResultExceptionMapper {
    @Override
    public TransceiveResult mapException(Exception e) {
        if(e instanceof UnresponsiveCardException) {
            return new TransceiveResult(TransceiveResult.RESULT_TAGLOST, null);
        }
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
