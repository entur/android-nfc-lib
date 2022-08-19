package no.entur.android.nfc.external.minova.reader;

import java.io.IOException;

import no.entur.android.nfc.tcpserver.CommandInputOutputThread;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;

public class MinovaCommands {

    public static final int EXTRA_SMALL = 0;
    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int LARGE = 3;
    public static final int EXTRA_LARGE = 4;
    public static final int EXTRA_EXTRA_LARGE = 5;

    protected static final String BUZZER = "BUZZER";
    private static final String LCDCLR = "LCDCLR";
    private static final String LCDSET = "LCDSET";
    private static final String LCDLOCK = "LCDLOCK";
    private static final String LCDUNLOCK = "LCDUNLOCK";
    private static final String DELAY = "DELAY";
    private static final String GET_TYPE = "GETTYPE";
    private static final String CAPDU = "CAPDU";

    protected final CommandInputOutputThread<String, String> reader;

    public MinovaCommands(CommandInputOutputThread<String, String> reader) {
        this.reader = reader;
    }

    public void buzz(int durationInMillis, int times) throws Exception {

        reader.write(McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(BUZZER, durationInMillis, times)
                .build()
        );
    }

    public String getType() throws IOException, InterruptedException {
        String getType = McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(GET_TYPE)
                .build();
        return reader.outputInput(getType);
    }

    public byte[] sendAdpu(byte[] command) throws IOException, InterruptedException {
        String minovaCommand = McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(CAPDU, ByteArrayHexStringConverter.byteArrayToHexString(command))
                .build();

        String response = reader.outputInput(minovaCommand);
        if (response.contains("NAK")) {
            throw new McrReaderException("Minova reader responded with NAK");
        }

        return ByteArrayHexStringConverter.hexStringToByteArray(response.substring(response.indexOf("=") + 1));
    }

    public void displayText(int xAxis, int yAxis, int font, String text) throws IOException {
        reader.write(McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(LCDCLR)
                .command(LCDSET, xAxis, yAxis, font, text)
                .build()
        );
    }

    public void displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis) throws IOException {
        reader.write(McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(LCDCLR)
                .command(LCDSET, xAxis, yAxis, font, text)
                .command(LCDLOCK)
                .command(DELAY, durationInMillis)
                .command(LCDUNLOCK)
                .build()
        );
    }

}
