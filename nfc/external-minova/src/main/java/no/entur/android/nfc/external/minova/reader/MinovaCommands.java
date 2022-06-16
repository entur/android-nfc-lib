package no.entur.android.nfc.external.minova.reader;

import no.entur.android.nfc.tcpserver.CommandInputOutputThread;

public class MinovaCommands {

    CommandInputOutputThread<String, String> reader;

    public MinovaCommands(CommandInputOutputThread<String, String> reader) {
        this.reader = reader;
    }

    private static final String SEPARATE_COMMAND = ",";
    private static final String SEPARATE_ARGUMENT = ";";

    private static final String BUZZER = "BUZZER";
    private static final String LCDCLR = "LCDCLR";
    private static final String LCDSET = "LCDSET";
    private static final String LCDLOCK = "LCDLOCK";
    private static final String LCDUNLOCK = "LCDUNLOCK";
    private static final String DELAY = "DELAY";

    public void buzz(int durationInMillis, int times) throws Exception {
        String commandSet = command("MCR04G-8E71") +
                command(BUZZER, durationInMillis, times);

        System.out.println(commandSet);
        reader.write(commandSet);
    }

    public void displayText(int xAxis, int yAxis, int font, String text) throws Exception {
        String commandSet = command(reader.getReaderId()) +
                command(LCDCLR) +
                command(LCDSET, xAxis, yAxis, font, text);

        System.out.println(commandSet);
        reader.write(commandSet);
    }

    public void displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis) throws Exception {
        String commandSet = command(reader.getReaderId()) +
                command(LCDCLR) +
                command(LCDSET, xAxis, yAxis, font, text) +
                command(LCDLOCK) +
                command(DELAY, durationInMillis) +
                command(LCDUNLOCK);

        System.out.println(commandSet);
        reader.write(commandSet);
    }

    private static <T> String command(String command, T... arguments) {
        StringBuilder argumentsBuilder = new StringBuilder();
        if (arguments.length > 0) {
            for (T argument : arguments) {
                argumentsBuilder.append(SEPARATE_ARGUMENT).append(argument);
            }
        }
        return command + argumentsBuilder + SEPARATE_COMMAND;
    }

}
