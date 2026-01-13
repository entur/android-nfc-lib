package no.entur.android.nfc.wrapper.tech.utils.bulk;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBulkTransceiveExecutor {

    public BulkTransceiveResponses transceive(BulkTransceiveCommands commands) throws IOException {
        BulkTransceiveResponses responses = new BulkTransceiveResponses();

        transceive(commands, responses);

        return responses;
    }

    public void transceive(BulkTransceiveCommands commands, BulkTransceiveResponses responses) throws IOException {
        List<PartialTranscieveResponseHandler> partialTranscieveResponseHandlers = commands.getPartialHandlers();
        Map<String, PartialTranscieveResponseHandler> handlers = new HashMap<>();
        for (PartialTranscieveResponseHandler h : partialTranscieveResponseHandlers) {
            handlers.put(h.getId(), h);
        }

        for (BulkTransceiveCommand item : commands.getItems()) {
            byte[] command = item.getCommand();

            byte[] response = transceive(command);

            String handlerId = item.getPartialHandlerId();
            if(handlerId != null) {
                PartialTranscieveResponseHandler handler = handlers.get(handlerId);
                if(handler == null) {
                    throw new IllegalStateException("Unknown partial response handler " + handlerId);
                }

                // is this a partial response?
                PartialTranscieveResponsePredicate predicate = handler.getPredicate();
                if(predicate.test(response)) {

                    // create reader for partial response (can be command-specific)
                    PartialTranscieveResponseReaderFactory factory = handler.getFactory();
                    PartialTranscieveResponseReader partialReader = factory.create(command, predicate);

                    // read untill no next command
                    do {
                        byte[] nextPartCommmand = partialReader.next(response);
                        if(nextPartCommmand == null) {
                            break;
                        }
                        response = transceive(nextPartCommmand);
                    } while(true);

                    response = partialReader.assemble();
                }
            }

            BulkTransceiveResponse bulkTransceiveResponse = new BulkTransceiveResponse();

            bulkTransceiveResponse.setId(item.getId());
            bulkTransceiveResponse.setResponse(response);

            responses.add(bulkTransceiveResponse);

            TranscieveResponsePredicate responsePredicate = item.getResponsePredicate();
            if(responsePredicate != null) {
                if(!responsePredicate.test(response)) {
                    break;
                }
            }
        }

    }

    public abstract byte[] transceive(byte[] data) throws IOException;

}
