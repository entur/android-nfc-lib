package no.entur.android.nfc.external.acs.reader.command.remote;

import no.entur.android.nfc.external.acs.reader.AcrReaderException;
import no.entur.android.nfc.external.remote.RemoteCommandException;
import no.entur.android.nfc.external.remote.RemoteCommandReader;
import no.entur.android.nfc.external.remote.RemoteCommandWriter;

public class AcrRemoteCommandWriter extends RemoteCommandWriter {

    protected RemoteCommandException createRemoteCommandException(Exception e) {
        return new AcrReaderException(e);
    }

    protected RemoteCommandException createRemoteCommandException(String string) {
        return new AcrReaderException(string);
    }

}
