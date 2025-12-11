package hwb.utilities.mqtt3.broker;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;

public class Mqtt3Extension implements IExtension {

    @Override
    public void decodeFrame(Framedata inputFrame) {
        //Nothing to do here
    }

    @Override
    public void encodeFrame(Framedata inputFrame) {
        //Nothing to do here
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
