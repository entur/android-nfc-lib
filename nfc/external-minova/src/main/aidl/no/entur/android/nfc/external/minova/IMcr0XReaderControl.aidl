package no.entur.android.nfc.external.minova;

interface IMcr0XReaderControl {

    byte[] buzz(int durationInMillis, int times);

    byte[] displayText(int xAxis, int yAxis, int font, String text);

    byte[] displayTextWithDuration(int xAxis, int yAxis, int font, String text, int durationInMillis);
}
