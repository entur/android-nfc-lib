package no.entur.android.nfc.websocket.android;

import no.entur.android.nfc.external.tag.TransceiveResultExceptionMapper;
import no.entur.android.nfc.wrapper.TransceiveResult;

public class WebSocketExceptionMapper implements TransceiveResultExceptionMapper {
    @Override
    public TransceiveResult mapException(Exception e) {
        return new TransceiveResult(TransceiveResult.RESULT_FAILURE, null);
    }
}
