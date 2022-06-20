package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import android.content.Intent;
import android.util.Log;

import org.nfctools.api.TagType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.entur.android.nfc.external.ExternalNfcReaderCallback;
import no.entur.android.nfc.external.acs.service.AbstractService;
import no.entur.android.nfc.external.minova.reader.McrCommandSetBuilder;
import no.entur.android.nfc.external.minova.reader.IMcr0XBinder;
import no.entur.android.nfc.external.minova.reader.McrReader;
import no.entur.android.nfc.external.minova.reader.MinovaCommands;
import no.entur.android.nfc.tcpserver.TerminatorCommandInput;
import no.entur.android.nfc.tcpserver.TerminatorCommandOutput;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public abstract class AbstractMinovaTcpService extends AbstractService implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String>  {

    private static final String LOG_TAG = AbstractMinovaTcpService.class.getName();

    private final List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    private final CommandServer server;

    private final Executor executor;

    // No port below 1025 can be used in the linux system.
    public AbstractMinovaTcpService(int port, int readers) {
        this.server = new CommandServer(this, port);
        this.executor = Executors.newScheduledThreadPool(readers);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server.start();
    }

    protected abstract void handleTag(TagType tag, byte[] atr, String uid, CommandInputOutputThread<String, String> reader);

    private static byte[] getAtr(byte[] ats) {
        // First byte of Smart cards ATR start with 3B or 3F, we use 3B. Next number should be 8.
        String atrString = "3B8";

        // Number of bytes sent including the one telling number of bytes.
        int length = ats[0] & 0xf;

        // ATS for DESFire starts with 0x75, saying that the next 3 bytes are interface bytes. The rest are historical bytes.
        int numOfHistoricalBytes = (length - 1) - 4;
        byte[] historicalBytes = new byte[numOfHistoricalBytes];
        System.arraycopy(ats, length - numOfHistoricalBytes, historicalBytes, 0, numOfHistoricalBytes);

        // The second byte's second nibble is number of historical bytes.
        atrString += numOfHistoricalBytes;

        // Third and fourth byte should be 0x80 0x01.
        atrString += "8001";

        // Next we add the actual historical bytes.
        atrString += ByteArrayHexStringConverter.toHexString(historicalBytes);

        byte[] atrWithoutChecksum = hexStringToByteArray(atrString);

        // Create checksum..
        byte[] checkSumBytes = new byte[atrWithoutChecksum.length - 1];
        System.arraycopy(atrWithoutChecksum, 1, checkSumBytes, 0, checkSumBytes.length);

        // Last byte of ATR is a checksum based on second through last byte before the checksum.
        atrString += createXorChecksum(checkSumBytes);

        // 0x3B + 0x8(numOfHistoricalBytes) + 0x80 0x01 + historicalBytes + checksum
        return hexStringToByteArray(atrString);
    }

    private static String createXorChecksum(byte[] bytes) {
        int checkSum = bytes[0];

        for (int i = 1; i < bytes.length; i++) {
            checkSum ^= bytes[i];
        }
        String chk = Integer.toHexString(checkSum);
        chk = chk.substring(chk.lastIndexOf('f') + 1);

        return chk;
    }


    @Override
    public void onServerSocketStart(int port) {
        Log.d(LOG_TAG, "Started server on port " + port);
    }

    @Override
    public void onServerSocketConnection(int port, Socket socket) throws IOException {
        Log.d(LOG_TAG, "Connection from " + port);

        CommandInput<String> input = new TerminatorCommandInput(McrCommandSetBuilder.COMMAND_SET_SEPERATOR, new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new TerminatorCommandOutput(McrCommandSetBuilder.COMMAND_SET_SEPERATOR, new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        CommandInputOutputThread<String, String> thread = new CommandInputOutputThread<>(this, socket, output, input);

        synchronized (clients) {
            clients.add(thread);
        }

        thread.start();
    }

    @Override
    public void onServerSocketClosed(int port, Exception e) {
        Log.d(LOG_TAG, "On server closed " + port, e);

        synchronized (clients) {
            for (CommandInputOutputThread<String, String> client : clients) {
                client.close();
                try {
                    client.join();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            clients.clear();
        }
    }

    @Override
    public void onReaderStart(CommandInputOutputThread<String, String> reader) {
        Log.d(LOG_TAG, "Reader " + reader.getReaderId() + " start");

        MinovaCommands commands = new MinovaCommands(reader);
        IMcr0XBinder mcrBinder = new IMcr0XBinder();
        mcrBinder.setCommands(commands);

        McrReader mcrReader = new McrReader(reader.getReaderId(), mcrBinder);

        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_OPENED);
        intent.putExtra(ExternalNfcReaderCallback.EXTRA_READER_CONTROL, mcrReader);

        sendBroadcast(intent);
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
        Log.d(LOG_TAG, "On reader command " + reader.getReaderId() + " " + input);

        if (input.contains("UID=")) { // XXX rather parse commands

            // run in seperate thread
            executor.execute(() -> {
                try {
                    String uid = input.substring(input.lastIndexOf("=") + 1);

                    MinovaCommands commands = new MinovaCommands(reader);

                    String response = commands.getType();

                    String atsString = response.substring((response.lastIndexOf(";") + 1));

                    byte[] atr = getAtr(hexStringToByteArray(atsString));
                    TagType tag = TagType.identifyTagType(atr);

                    handleTag(tag, atr, uid, reader);
                } catch(Exception e) {
                    Log.d(LOG_TAG, "Problem getting tag type");
                }
            });
        } else {
            Log.d(LOG_TAG, "Ignoring reader " + reader.getReaderId() + " command " + input);
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
        Log.d(LOG_TAG, "Reader " + reader.getReaderId() + " closed");

        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);

        sendBroadcast(intent);
    }

}
