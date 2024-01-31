package no.entur.android.nfc.external.minova.reader;

import java.net.Socket;

import no.entur.android.nfc.external.service.tag.DefaultTagProxy;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;

public class MinovaCommandInputOutputThread extends CommandInputOutputThread<String, String> {

    private TagProxy currentTagProxy;

    public MinovaCommandInputOutputThread(Listener<String, String> listener, Socket clientSocket, CommandOutput<String> out, CommandInput<String> in) {
        super(listener, clientSocket, out, in);
    }

    public void setCurrentTagProxy(TagProxy currentTagProxy) {
        this.currentTagProxy = currentTagProxy;
    }

    public TagProxy getCurrentTagProxy() {
        return currentTagProxy;
    }
}
