package no.entur.android.nfc.external.minova.reader;

public class McrCommandSetBuilder {

    // The Terminal sends the following to Server:
    // MCR02-8AC64C,UID=1255CCF0,IO=0F
    // The Server may send the following to Terminal:
    // MCR02-8AC64C,BUZZER;500;1,LCDCLR,LCDSET;0;0;0;Test1,LCDCLR,LCDSET;0;10;0;Hello World!
    // or
    // MCR02-8AC64C,BUZZER;500;1,LCDCLR,LCDSET;0;0;0;Test1,LCDCLR,LCDSET;0;10;0;Hello World!,RELAY1=500

    public static final char COMMAND_SET_SEPERATOR = '\n';
    public static final char COMMAND_SEPERATOR = ',';
    public static final char ARGUMENT_SEPERATOR = ';';

    public static McrCommandSetBuilder newInstance(String readerId) {
        return new McrCommandSetBuilder().command(readerId);
    }

    private StringBuilder argumentsBuilder = new StringBuilder(128);

    public McrCommandSetBuilder command(String command, Object... arguments) {
        argumentsBuilder.append(command);

        for (Object argument : arguments) {
            append(argument);
        }
        argumentsBuilder.append(COMMAND_SEPERATOR);
        return this;
    }

    private void append(Object object) {
        argumentsBuilder.append(ARGUMENT_SEPERATOR);
        argumentsBuilder.append(object);
    }

    public String build() {
        return argumentsBuilder.toString();
    }

}
