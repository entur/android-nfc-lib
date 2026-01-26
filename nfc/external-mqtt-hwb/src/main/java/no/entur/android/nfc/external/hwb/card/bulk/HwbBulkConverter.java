package no.entur.android.nfc.external.hwb.card.bulk;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.Command;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import hwb.utilities.validators.nfc.apdu.receive.Result;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommand;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveCommands;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveResponse;
import no.entur.android.nfc.wrapper.tech.utils.bulk.BulkTransceiveResponses;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponseHandler;
import no.entur.android.nfc.wrapper.tech.utils.bulk.PartialTransceiveResponsePredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.TransceiveResponsePredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.apdu.ApduPartialTransceiveResponsePredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.apdu.ApduTransceiveReponseStatusPredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.desfire.NativeMifareDesfireEV1PartialTransceiveResponsePredicate;
import no.entur.android.nfc.wrapper.tech.utils.bulk.desfire.NativeMifareDesfireEV1TransceiveResponseStatusPredicate;

/**
 *
 * HWB unfortunately mixes up expected status (i.e. should we continue to execute commands)
 * and partial responses (should be read more command results before the next command).
 *
 */

public class HwbBulkConverter {

    private UUID traceId;
    private String deviceId;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public TransmitSchema convert(BulkTransceiveCommands commands) {
        TransmitSchema schema = new TransmitSchema();

        TransmitSchema.ApduType apduType = detectApduType(commands);

        schema.setApduType(apduType);

        if(apduType == TransmitSchema.ApduType.DESFIRE) {
            schema.setCommand(convertDesfireCommands(commands));
        } else {
            schema.setCommand(convertApduCommands(commands));
        }

        schema.setTraceId(traceId);
        schema.setDeviceId(deviceId);

        schema.setEventTimestamp(new Date());
        schema.setTransceiveId(UUID.randomUUID());

        return schema;
    }


    private List<Command> convertDesfireCommands(BulkTransceiveCommands commands) {

        List<BulkTransceiveCommand> items = commands.getItems();
        List<PartialTransceiveResponseHandler> partialHandlers = commands.getPartialHandlers();

        Map<String, PartialTransceiveResponseHandler> handlerMap = new HashMap<>();
        for (PartialTransceiveResponseHandler partialHandler : partialHandlers) {
            handlerMap.put(partialHandler.getId(), partialHandler);
        }

        List<Command> result = new ArrayList<>(items.size());
        for (BulkTransceiveCommand item : items) {

            Command c = new Command();
            c.setCommandId(item.getId());
            c.setFrame(ByteArrayHexStringConverter.toHexString(item.getCommand()));

            // If expStatus=0x00 or empty and first bytes in response indicates more data (0xAF)
            // the device should fetch all data until end and give result as concatinated byte array.
            // If expStatus=0xAF is transmitted it is not handled automatically by the device.

            if(item.hasResponsePredicate()) {
                // AF handling implicit unless expected status is 0xAF

                TransceiveResponsePredicate p = item.getResponsePredicate();
                if(p instanceof NativeMifareDesfireEV1TransceiveResponseStatusPredicate) {
                    NativeMifareDesfireEV1TransceiveResponseStatusPredicate predicate = (NativeMifareDesfireEV1TransceiveResponseStatusPredicate)p;

                    int expectedStatus = predicate.getStatus();

                    if(expectedStatus == 0xAF) {
                        if(item.hasPartialHandlerId()) {
                            throw new IllegalStateException("Cannot handle partial responses for expected status 0xAF");
                        }
                    } else if(expectedStatus == 0x00) {
                        // reader will handle AF
                        // check that this is as expected
                        if (item.hasPartialHandlerId()) {
                            PartialTransceiveResponseHandler handler = handlerMap.get(item.getPartialHandlerId());

                            PartialTransceiveResponsePredicate partialPredicate = handler.getPredicate();
                            if(partialPredicate instanceof NativeMifareDesfireEV1PartialTransceiveResponsePredicate) {
                                // pass
                            } else {
                                throw new IllegalStateException("Unexpected partial response predicate type " + partialPredicate.getClass().getName());
                            }
                        } else {
                            throw new IllegalStateException("Partial responses are always handled for expected status 0x00, but not expected in input.");
                        }
                    }

                    c.setExpStatus("0x" + ByteArrayHexStringConverter.byteToHexString((byte)(expectedStatus & 0xFF)));
                } else {
                    throw new IllegalStateException("Unknown response predicate type " + p.getClass().getName());
                }
            } else {
                // reader will handle AF
                // check that this is as expected
                if(item.hasPartialHandlerId()) {
                    PartialTransceiveResponseHandler handler = handlerMap.get(item.getPartialHandlerId());
                    // no expected status, so if we do nothing, AF is handled

                    PartialTransceiveResponsePredicate partialPredicate = handler.getPredicate();
                    if(partialPredicate instanceof NativeMifareDesfireEV1PartialTransceiveResponsePredicate) {
                        // pass
                    } else {
                        throw new IllegalStateException("Unexpected partial response predicate type " + partialPredicate.getClass().getName());
                    }
                } else {
                    throw new IllegalStateException("Reader handles partial responses if no expected status is specified, but no partial response handling expected in input.");
                }
            }

            result.add(c);
        }

        return result;
    }

    private List<Command> convertApduCommands(BulkTransceiveCommands commands) {
        List<BulkTransceiveCommand> items = commands.getItems();
        List<PartialTransceiveResponseHandler> partialHandlers = commands.getPartialHandlers();

        Map<String, PartialTransceiveResponseHandler> handlerMap = new HashMap<>();
        for (PartialTransceiveResponseHandler partialHandler : partialHandlers) {
            handlerMap.put(partialHandler.getId(), partialHandler);
        }

        List<Command> result = new ArrayList<>(items.size());
        for (BulkTransceiveCommand item : items) {

            Command c = new Command();
            c.setCommandId(item.getId());
            c.setFrame(ByteArrayHexStringConverter.toHexString(item.getCommand()));

            // TODO should expectee status be one or two bytes? APDU has two status bytes (sw1 + sw2)
            // TODO is this right? Docs are lacking. Now implemented as desfire ev1 with apdu.

            if(item.hasResponsePredicate()) {
                // AF handling implicit
                TransceiveResponsePredicate p = item.getResponsePredicate();
                if(p instanceof ApduTransceiveReponseStatusPredicate) {
                    ApduTransceiveReponseStatusPredicate predicate = (ApduTransceiveReponseStatusPredicate)p;

                    int sw1 = predicate.getSw1();
                    int sw2 = predicate.getSw2();

                    if(sw1 == 0x90 && sw2 == 0xAF) {
                        if(item.hasPartialHandlerId()) {
                            throw new IllegalStateException("Cannot handle partial responses for expected status 0xAF");
                        }
                    } else if(sw1 == 0x90 && sw2 == 0x00) {
                        // reader will handle 90AF
                        // check that this is as expected
                        if (item.hasPartialHandlerId()) {
                            PartialTransceiveResponseHandler handler = handlerMap.get(item.getPartialHandlerId());

                            PartialTransceiveResponsePredicate partialPredicate = handler.getPredicate();
                            if(partialPredicate instanceof ApduPartialTransceiveResponsePredicate) {
                                if(predicate.getSw1() == 0x90 && predicate.getSw2() == 0xAF) {
                                    // pass
                                } else {
                                    throw new IllegalStateException("Cannot handle partial response predicate other than 0x90AF");
                                }
                            } else {
                                throw new IllegalStateException("Unexpected partial response predicate type " + partialPredicate.getClass().getName());
                            }
                        } else {
                            throw new IllegalStateException("Reader handles partial responses for expected status 0x9000, but no partial handling expected in input.");
                        }
                    }
                    c.setExpStatus("0x" + ByteArrayHexStringConverter.byteToHexString((byte)(sw1 & 0xFF)) + ByteArrayHexStringConverter.byteToHexString((byte)(sw2 & 0xFF)));
                } else {
                    throw new IllegalStateException("Unknown response predicate " + p.getClass().getName());
                }
            } else {
                // reader will handle 90AF
                // check that this is as expected

                if(item.hasPartialHandlerId()) {
                    PartialTransceiveResponseHandler handler = handlerMap.get(item.getPartialHandlerId());

                    PartialTransceiveResponsePredicate p = handler.getPredicate();

                    // no expected status, so if we do nothing, AF is handled
                    // check that this is as expected
                    if(p instanceof ApduPartialTransceiveResponsePredicate) {
                        ApduPartialTransceiveResponsePredicate predicate = (ApduPartialTransceiveResponsePredicate)p;

                        if(predicate.getSw1() == 0x90 && predicate.getSw2() == 0xAF) {
                            // pass
                        } else {
                            throw new IllegalStateException("Cannot handle partial response predicate other than 0x90AF");
                        }
                    } else {
                        throw new IllegalStateException("Unexpected partial response predicate type " + p.getClass().getName());
                    }
                } else {
                    throw new IllegalStateException("Reader handles partial responses if no expected status is specified, but no partial handling expected in input.");
                }
            }

            result.add(c);
        }

        return result;
    }

    private TransmitSchema.ApduType detectApduType(BulkTransceiveCommands commands) {
        List<BulkTransceiveCommand> items = commands.getItems();

        for (BulkTransceiveCommand item : items) {

            byte[] command = item.getCommand();
            if(command.length < 4) {
                return TransmitSchema.ApduType.DESFIRE;
            }
            // is this already an APDU?
            int cls = command[0] & 0xFF;

            if ((cls == 0x00 || cls == 0x90 || cls == 0xFF) && isApduLength(command)) {
                return TransmitSchema.ApduType.ISO_7816;
            }
            return TransmitSchema.ApduType.DESFIRE;
        }

        return TransmitSchema.ApduType.ISO_7816;
    }


    private boolean isApduLength(byte[] data) {
        if (data.length >= 5) {

            // CLA (Class): 1 byte.
            // INS (Instruction): 1 byte.
            // P1 (Parameter 1): 1 byte.
            // P2 (Parameter 2): 1 byte.
            // Lc (Length of Command Data): Optional (1 or 3 bytes).
            // Data Field: Optional, length specified by Lc.
            // Le (Length Expected): Optional (1 or 3 bytes).

            int lengthOfCommandData = data[4] & 0xFF;

            // no Length expected
            if (lengthOfCommandData == data.length - 5) {
                return true;
            }
            // 1 byte Length expected
            if (lengthOfCommandData + 1 == data.length - 5) {
                return true;
            }
            // 3 byte Length expected
            if (lengthOfCommandData + 3 == data.length - 5) {
                return true;
            }

            return false;
        }
        return true;
    }

    public BulkTransceiveResponses convert(ReceiveSchema receiveSchema) {

        BulkTransceiveResponses r = new BulkTransceiveResponses();

        List<Result> responses = receiveSchema.getResult();

        for (Result respons : responses) {

            BulkTransceiveResponse resultResponse = new BulkTransceiveResponse();
            resultResponse.setId(respons.getCommandId());

            String frame = respons.getFrame();
            String hex = frame.substring(2);// remove 0x

            resultResponse.setResponse(ByteArrayHexStringConverter.hexStringToByteArray(hex));

            r.add(resultResponse);
        }


        return r;
    }
}
