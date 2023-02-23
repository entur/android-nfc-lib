package no.entur.android.nfc.external.minova.reader;

parcelable MinovaDisplayText;

interface IMcr0XReaderControl {

    byte[] getIp();

    byte[] buzz(int durationInMillis, int times);

    byte[] displayText(int xAxis, int yAxis, int font, String text);

    byte[] displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis);

    byte[] displayMultilineTextWithDuration(in List<MinovaDisplayText> displayTexts, int durationInMillis);
}
