package no.entur.android.nfc.external.minova.reader;

import android.os.Parcel;
import android.os.Parcelable;

public class MinovaDisplayText implements Parcelable {
    private final int xAxis;
    private final int yAxis;
    private final int font;
    private final String text;

    public MinovaDisplayText(int xAxis, int yAxis, int font, String text) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.font = font;
        this.text = text;
    }

    public int getXAxis() {
        return xAxis;
    }

    public int getYAxis() {
        return yAxis;
    }

    public int getFont() {
        return font;
    }

    public String getText() {
        return text;
    }

    protected MinovaDisplayText(Parcel in) {
        xAxis = in.readInt();
        yAxis = in.readInt();
        font = in.readInt();
        text = in.readString();
    }

    public static final Creator<MinovaDisplayText> CREATOR = new Creator<MinovaDisplayText>() {
        @Override
        public MinovaDisplayText createFromParcel(Parcel in) {
            return new MinovaDisplayText(in);
        }

        @Override
        public MinovaDisplayText[] newArray(int size) {
            return new MinovaDisplayText[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(xAxis);
        dest.writeInt(yAxis);
        dest.writeInt(font);
        dest.writeString(text);
    }
}

