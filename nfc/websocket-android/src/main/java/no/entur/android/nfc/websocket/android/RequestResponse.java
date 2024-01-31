package no.entur.android.nfc.websocket.android;

import no.entur.android.nfc.websocket.messages.NfcMessage;

public class RequestResponse<Request extends NfcMessage, Response extends NfcMessage> {

    private final int id;

    public RequestResponse(int id, Request request) {
        this.id = id;
        this.request = request;
    }

    private final Request request;
    private volatile Response response;

    public Request getRequest() {
        return request;
    }

    public void setResponse(Response response) {
        this.response = response;
    }


}
