package no.entur.android.nfc.external.minova.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;

import org.junit.Test;
import org.nfctools.api.TagType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import no.entur.android.nfc.external.minova.reader.MinovaCommandInputOutputThread;
import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.external.service.tag.DefaultTagProxyStore;
import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;

/**
 * Verify that a tag left field broadcast is sent exactly once when a tag is lost.
 */

public class MinovaTagLostTest {

    private static final String NFC_PERMISSION = "android.permission.NFC";

    @Test
    public void testTransceiveFailureSendsTagLeftField() throws Exception {
        MinovaCommandInputOutputThread reader = newReader();
        // closed connection, so any command fails
        reader.close();

        TagProxy tagProxy = new DefaultTagProxyStore().add(-1, new ArrayList<>());
        reader.setCurrentTagProxy(tagProxy);

        Context context = mock(Context.class);
        MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(reader, context);
        wrapper.setTagProxy(tagProxy);

        assertThrows(IOException.class, () -> wrapper.transceive(new byte[]{0x00}));
        verify(context).sendBroadcast(any(Intent.class), eq(NFC_PERMISSION));

        assertFalse(tagProxy.isPresent());
        assertNull(reader.getCurrentTagProxy());

        // only one broadcast per tag
        assertThrows(IOException.class, () -> wrapper.transceive(new byte[]{0x00}));
        verify(context, times(1)).sendBroadcast(any(Intent.class), eq(NFC_PERMISSION));
    }

    @Test
    public void testNewTagSendsTagLeftFieldForPreviousTag() throws Exception {
        MinovaCommandInputOutputThread reader = newReader();

        TagProxy previous = new DefaultTagProxyStore().add(-1, new ArrayList<>());
        reader.setCurrentTagProxy(previous);

        MinovaService service = spy(new MinovaService(0, 1));
        doNothing().when(service).sendBroadcast(any(Intent.class), anyString());

        MinovaTagType tag = new MinovaTagType();
        tag.setTagType(TagType.MIFARE_ULTRALIGHT);

        service.handleTag(tag, "04112233445566", reader);

        verify(service, times(1)).sendBroadcast(any(Intent.class), eq(NFC_PERMISSION));
        assertFalse(previous.isPresent());
        assertNull(reader.getCurrentTagProxy());
    }

    private static MinovaCommandInputOutputThread newReader() {
        Socket socket = new Socket() {
            @Override
            public InetAddress getInetAddress() {
                return InetAddress.getLoopbackAddress();
            }
        };

        CommandOutput<String> output = new CommandOutput<String>() {
            @Override
            public void write(String command) {
            }

            @Override
            public void close() {
            }
        };

        CommandInput<String> input = new CommandInput<String>() {
            @Override
            public String read() throws IOException {
                throw new IOException("Not connected");
            }

            @Override
            public void close() {
            }
        };

        CommandInputOutputThread.Listener<String, String> listener = new CommandInputOutputThread.Listener<String, String>() {
            @Override
            public void onReaderStart(CommandInputOutputThread<String, String> reader) {
            }

            @Override
            public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
            }

            @Override
            public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
            }
        };

        return new MinovaCommandInputOutputThread(listener, socket, output, input);
    }
}
