package no.entur.android.nfc.external.atr210.schema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusDeserializer extends JsonDeserializer<List<Status>> {

    @Override
    public List<Status> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // Split the string by comma and trim whitespace from each element

        List<Status> values = new ArrayList<>();

        for (String s : value.split(",")) {
            if(!s.isEmpty()) {
                values.add(Status.valueOf(s));
            }
        }

        return values;
    }
}