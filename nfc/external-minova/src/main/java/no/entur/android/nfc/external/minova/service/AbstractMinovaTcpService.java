package no.entur.android.nfc.external.minova.service;

import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.entur.android.nfc.external.minova.reader.MinovaCommandInputOutputThread;
import no.entur.android.nfc.external.service.AbstractService;
import no.entur.android.nfc.external.minova.reader.McrCommandSetBuilder;
import no.entur.android.nfc.external.minova.reader.IMcr0XBinder;
import no.entur.android.nfc.external.minova.reader.McrReader;
import no.entur.android.nfc.external.minova.reader.MinovaCommands;
import no.entur.android.nfc.external.minova.reader.MinovaReaderTechnology;
import no.entur.android.nfc.tcpserver.TerminatorCommandInput;
import no.entur.android.nfc.tcpserver.TerminatorCommandOutput;
import no.entur.android.nfc.tcpserver.CommandInput;
import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.tcpserver.CommandOutput;
import no.entur.android.nfc.tcpserver.CommandServer;

public abstract class AbstractMinovaTcpService extends AbstractService implements CommandServer.Listener, CommandInputOutputThread.Listener<String, String>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMinovaTcpService.class);

    public static final String EXTRA_IP = AbstractMinovaTcpService.class.getName() + ".extra.IP";

    private final List<MinovaCommandInputOutputThread> clients = new ArrayList<>();

    private final CommandServer server;

    private MinovaTagTypeDetector tagTypeDetector = new MinovaTagTypeDetector();

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

        this.binder.setReaderTechnology(new MinovaReaderTechnology());
    }

    protected abstract void handleTag(MinovaTagType tag, String uid, CommandInputOutputThread<String, String> reader);

    @Override
    public void onServerSocketStart(int port) {
        LOGGER.debug("Started server on port " + port);
    }

    @Override
    public void onServerSocketConnection(int port, Socket socket) throws IOException {
        LOGGER.debug("Connection from " + port + ". IP is " + socket.getInetAddress());

        CommandInput<String> input = new TerminatorCommandInput(McrCommandSetBuilder.COMMAND_SET_SEPERATOR, new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        CommandOutput<String> output = new TerminatorCommandOutput(McrCommandSetBuilder.COMMAND_SET_SEPERATOR, new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        MinovaCommandInputOutputThread thread = new MinovaCommandInputOutputThread(this, socket, output, input);

        synchronized (clients) {
            clients.add(thread);
        }

        thread.start();
    }

    @Override
    public void onServerSocketClosed(int port, Exception e) {
        LOGGER.debug("On server closed " + port, e);

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
        LOGGER.debug("Reader " + reader.getReaderId() + " start");

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
        LOGGER.debug("On reader command " + reader.getReaderId() + " " + input);

        if (input.contains("UID=")) { // XXX rather parse commands
            // run in seperate thread
            executor.execute(() -> {
                try {
                    String uid = input.substring(input.lastIndexOf("=") + 1);

                    MinovaCommands commands = new MinovaCommands(reader);

                    MinovaTagType tagType = tagTypeDetector.getTagType(commands);

                    handleTag(tagType, uid, reader);
                } catch(Exception e) {
                    LOGGER.debug("Problem getting tag type");
                }
            });
        } else {
            LOGGER.debug("Ignoring reader " + reader.getReaderId() + " command " + input);
        }
    }

    @Override
    public void onReaderClosed(CommandInputOutputThread<String, String> reader, Exception e) {
        LOGGER.debug("Reader " + reader.getReaderId() + " closed");

        Intent intent = new Intent();
        intent.setAction(ExternalNfcReaderCallback.ACTION_READER_CLOSED);
        intent.putExtra(EXTRA_IP, reader.getIp());

        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(server != null) {
            try {
                server.stop();
            } catch (IOException e) {
                LOGGER.debug("Problem stopping server", e);
            }
        }
    }

}
