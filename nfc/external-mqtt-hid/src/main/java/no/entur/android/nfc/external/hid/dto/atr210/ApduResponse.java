package no.entur.android.nfc.external.hid.dto.atr210;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApduResponse {

    private int commandId;
    private String frame;
    private String response;

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApduResponse response1 = (ApduResponse) o;
        return commandId == response1.commandId && Objects.equals(frame, response1.frame) && Objects.equals(response, response1.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, frame, response);
    }

    @Override
    public String toString() {
        return "ApduResponse{" +
                "commandId=" + commandId +
                ", frame='" + frame + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
