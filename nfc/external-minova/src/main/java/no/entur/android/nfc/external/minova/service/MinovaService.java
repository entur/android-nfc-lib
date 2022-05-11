package no.entur.android.nfc.external.minova.service;

import static no.entur.android.nfc.util.ByteArrayHexStringConverter.hexStringToByteArray;

import org.nfctools.api.TagType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.acs.service.AbstractService;
import no.entur.android.nfc.external.minova.reader.MinovaIsoDepWrapper;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaService extends AbstractService implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String> {

    // No port below 1025 can be used in the linux system.
    private final int port = 1025;

    private String uid;

    private CommandServer server;
    private List<CommandInputOutputThread<String, String>> clients = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        server = new CommandServer(this, port);
        server.start();
    }

    @Override
    public void onServerSocketStart(int port) {
        System.out.println("On server start " + port);
    }

    @Override
    public void onServerSocketConnection(int port, Socket socket) throws IOException {
        CommandInput<String> input = new CommaCommandInput(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new CommaCommandOutput(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        CommandInputOutputThread thread = new CommandInputOutputThread(this, socket, output, input);

        synchronized (clients) {
            clients.add(thread);
        }

        thread.start();
    }

    @Override
    public void onServerSocketClosed(int port, Exception e) {
        System.out.println("On server closed " + port + ": " + e);
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
        System.out.println("On reader connect " + reader);
    }

    @Override
    public void onReaderCommand(CommandInputOutputThread<String, String> reader, String input) {
        System.out.println("On reader command " + reader + " " + input);
        String readerIdWithComma = input.substring(0, input.indexOf(",") + 1);

        if (input.contains("UID")) {
            System.out.println("Contains UID!");
            try {
                uid = input.substring(input.lastIndexOf("=") + 1);
                /*String result = reader.outputInput(readerIdWithComma + "GETTYPE");
                System.out.println(result);
                getTag(result.substring((result.lastIndexOf(';')+1)), uid);*/
                reader.write(readerIdWithComma + ", GETTYPE");
            } catch (Exception e) {
                System.out.print(e.getLocalizedMessage());
            }
        }

        if (input.contains("CARDTYPE")) {
            String atsString = input.substring((input.lastIndexOf(";") + 1));
            getTag(atsString, uid);
        }

    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
        System.out.println("On reader disconnected " + reader + ": " + e);
        synchronized (clients) {
            clients.remove(reader);
        }
    }

    private void getTag(String atsString, String uid) {
        byte[] ats = hexStringToByteArray(atsString);
        byte[] atr = getAtr(ats);

        TagType tag = TagType.identifyTagType(atr);

        if (tag == TagType.DESFIRE_EV1) {
            MinovaIsoDepWrapper wrapper = new MinovaIsoDepWrapper(clients.get(0));
            mifareDesfireTagServiceSupport.desfire(0, atr, wrapper, hexStringToByteArray(uid));
        }
    }

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

        System.out.println(atrString);

        // 0x3B + 0x8(numOfHistoricalBytes) + 0x80 0x01 + historicalBytes + checksum
        return hexStringToByteArray(atrString);
    }

    private static String createXorChecksum(byte[] bytes) {
        int checkSum = bytes[0];
        System.out.println(ByteArrayHexStringConverter.toHexString(bytes));

        for (int i = 1; i < bytes.length; i++) {
            checkSum ^= bytes[i];
        }
        String chk = Integer.toHexString(checkSum);
        chk = chk.substring(chk.lastIndexOf('f') + 1);

        return chk;
    }

}
