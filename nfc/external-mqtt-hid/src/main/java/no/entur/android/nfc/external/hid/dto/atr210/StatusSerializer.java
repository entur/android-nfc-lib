package no.entur.android.nfc.external.hid.dto.atr210;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.entur.android.nfc.external.hid.intent.NfcCardStatus;

public class StatusSerializer extends JsonSerializer<List<NfcCardStatus>> {

    @Override
    public void serialize(List<NfcCardStatus> values, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (NfcCardStatus value : values) {
            builder.append(value.toString());
            builder.append(",");
        }

        if(!values.isEmpty()) {
            builder.setLength(builder.length() - 1);
        }

        gen.writeString(builder.toString());
    }
}