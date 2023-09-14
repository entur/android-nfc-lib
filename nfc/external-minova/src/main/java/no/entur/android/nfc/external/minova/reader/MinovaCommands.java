package no.entur.android.nfc.external.minova.reader;

import java.io.IOException;
import java.util.List;

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

    public String getIp() {
        return reader.getIp();
    }

    public void buzz(int durationInMillis, int times) throws Exception {

        reader.write(McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(BUZZER, durationInMillis, times)
                .build()
        );
    }


    /**
     * Execute the GETTYPE command. The answer is CARDTYPE=ATQ;SAK;ATS
     *
     * ISO4 DESFIRE Card Example
     * -> MCR04G-50F2,UID=807644D24E2904
     * <- MCR04G-50F2,GETTYPE
     * -> MCR04G-50F2,CARDTYPE=0344;20;067577810280
     *
     * ISO3 Card Example
     *
     * -> MCR04G-50F2,UID=16FE1E24
     * <- MCR04G-50F2,GETTYPE
     * -> MCR04G-50F2,CARDTYPE=0004;08;00
     *
     * @return result
     * @throws IOException
     * @throws InterruptedException
     */

    public String getType() throws IOException, InterruptedException {
        String getType = McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(GET_TYPE)
                .build();

        // MCR04G-4FBB,CARDTYPE=0344;20;067577810280
        String response = reader.outputInput(getType);

        return response.substring(response.indexOf("=") + 1);
    }

    public byte[] sendAdpu(byte[] command) throws IOException, InterruptedException {
        String minovaCommand = McrCommandSetBuilder.newInstance(reader.getReaderId())
                .command(CAPDU, ByteArrayHexStringConverter.byteArrayToHexString(command))
                .build();

        String response = reader.outputInput(minovaCommand);
        if (!response.startsWith("RAPDU=") || response.endsWith(",NAK")) {
            throw new McrReaderException("Minova reader responded with '" + response + "'");
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

    public void displayMultilineTextWithDuration(List<MinovaDisplayText> displayTexts, int durationInMillis) throws IOException {
        McrCommandSetBuilder builder = McrCommandSetBuilder.newInstance(reader.getReaderId()).command(LCDCLR);
        for (MinovaDisplayText displayText : displayTexts) {
            builder.command(
                    LCDSET,
                    displayText.getXAxis(),
                    displayText.getYAxis(),
                    displayText.getFont(),
                    displayText.getText()
            );
        }
        reader.write(builder.command(LCDLOCK)
                .command(DELAY, durationInMillis)
                .command(LCDUNLOCK)
                .build());
    }
}
