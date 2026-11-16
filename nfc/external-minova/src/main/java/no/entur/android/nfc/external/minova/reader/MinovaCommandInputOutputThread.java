package no.entur.android.nfc.external.minova.reader;

import java.net.Socket;

import no.entur.android.nfc.external.service.tag.DefaultTagProxy;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;

public class MinovaCommandInputOutputThread extends CommandInputOutputThread<String, String> {

    private static final String UID = "UID=";

    /**
     * Get the UID from a card message, i.e. MCR02-8AC64C,UID=1255CCF0,IO=0F
     *
     * @param input message from reader
     * @return the UID, or null if not a card message
     */

    public static String parseUid(String input) {
        String[] commands = input.split(String.valueOf(McrCommandSetBuilder.COMMAND_SEPERATOR));
        for (String command : commands) {
            String trimmed = command.trim();
            if(trimmed.startsWith(UID)) {
                String uid = trimmed.substring(UID.length()).trim();
                if(uid.isEmpty()) {
                    return null;
                }
                return uid;
            }
        }
        return null;
    }

    public static boolean isUidMessage(String input) {
        return parseUid(input) != null;
    }

    private final Object currentTagProxyLock = new Object();

    private TagProxy currentTagProxy;

    // serialize tag handling for this reader
    private final Object tagLock = new Object();

    public MinovaCommandInputOutputThread(Listener<String, String> listener, Socket clientSocket, CommandOutput<String> out, CommandInput<String> in) {
        super(listener, clientSocket, out, in);
    }

    @Override
    protected boolean isResponse(String input) {
        // a new card might be presented while waiting for a response to some command
        return !isUidMessage(input);
    }

    public void setCurrentTagProxy(TagProxy currentTagProxy) {
        synchronized (currentTagProxyLock) {
            this.currentTagProxy = currentTagProxy;
        }
    }

    public TagProxy getCurrentTagProxy() {
        synchronized (currentTagProxyLock) {
            return currentTagProxy;
        }
    }

    /**
     * Get and clear the current tag proxy.
     *
     * @return the current tag proxy, or null if none
     */

    public TagProxy takeCurrentTagProxy() {
        synchronized (currentTagProxyLock) {
            TagProxy tagProxy = this.currentTagProxy;
            this.currentTagProxy = null;
            return tagProxy;
        }
    }

    /**
     * Clear the current tag proxy, if it is the expected one.
     *
     * @param expected expected tag proxy
     * @return true if the current tag proxy was the expected one
     */

    public boolean clearCurrentTagProxy(TagProxy expected) {
        synchronized (currentTagProxyLock) {
            if(this.currentTagProxy == expected) {
                this.currentTagProxy = null;
                return true;
            }
            return false;
        }
    }

    /**
     * Mark a tag as no longer present, so that only one tag left field event is sent for it.
     *
     * @param tagProxy tag proxy
     * @return true if the tag was present (i.e. was not already marked as lost)
     */

    public boolean markTagLost(TagProxy tagProxy) {
        synchronized (currentTagProxyLock) {
            if(!tagProxy.isPresent()) {
                return false;
            }
            tagProxy.setPresent(false);
            return true;
        }
    }

    public Object getTagLock() {
        return tagLock;
    }
}
