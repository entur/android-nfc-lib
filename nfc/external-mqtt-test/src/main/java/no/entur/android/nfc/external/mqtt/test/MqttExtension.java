package no.entur.android.nfc.external.mqtt.test;

import android.util.Log;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;

public class MqttExtension implements IExtension {

    private static final String LOG_TAG = MqttExtension.class.getName();

    @Override
    public void decodeFrame(Framedata inputFrame) throws InvalidDataException {
        //Nothing to do here
        Log.d(LOG_TAG, "decodeFrame");
    }

    @Override
    public void encodeFrame(Framedata inputFrame) {
        //Nothing to do here
        Log.d(LOG_TAG, "encodeFrame");
    }

    @Override
    public boolean acceptProvidedExtensionAsServer(String inputExtension) {
        return true;
    }

    @Override
    public boolean acceptProvidedExtensionAsClient(String inputExtension) {
        return true;
    }

    @Override
    public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
        Log.d(LOG_TAG, "isFrameValid");
        if (inputFrame.isRSV1() || inputFrame.isRSV2() || inputFrame.isRSV3()) {
            throw new InvalidFrameException(
                    "bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: "
                            + inputFrame.isRSV3());
        }
    }

    @Override
    public String getProvidedExtensionAsClient() {
        return "";
    }

    @Override
    public String getProvidedExtensionAsServer() {
        return "";
    }

    @Override
    public IExtension copyInstance() {
        return new DefaultExtension();
    }

    public void reset() {
        //Nothing to do here. No internal stats.
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass();
    }
}
