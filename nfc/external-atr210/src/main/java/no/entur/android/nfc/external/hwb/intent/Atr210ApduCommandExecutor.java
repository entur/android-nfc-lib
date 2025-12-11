package no.entur.android.nfc.external.hwb.intent;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import hwb.utilities.validators.nfc.apdu.deviceId.transmit.Command;
import hwb.utilities.validators.nfc.apdu.deviceId.transmit.TransmitSchema;
import hwb.utilities.validators.nfc.apdu.receive.ReceiveSchema;
import hwb.utilities.validators.nfc.apdu.receive.Result;
import no.entur.android.nfc.util.ByteArrayHexStringConverter;
import no.entur.android.nfc.wrapper.tech.IsoDep;

public class Atr210ApduCommandExecutor {

	private static final String LOG_TAG = "Apdu";

	private static final byte STATUS_ADDITIONAL_FRAME = (byte) 0xAF;

    private static final byte[] ADDITIONAL_FRAME_NATIVE_DESFIRE = new byte[] { (byte) 0xAF };

    private static final byte[] ADDITIONAL_FRAME_ISO7816 = new byte[] {(byte) 0x90, (byte) 0xAF, 0x00, 0x00, 0x00 };

    public static final byte STATUS_OK = (byte) 0x91;


    public ReceiveSchema executeCommandSet(IsoDep isoDep, TransmitSchema commandSet, boolean logCommands) throws IOException {
        if(commandSet.getApduType() == TransmitSchema.ApduType.DESFIRE) {
            return executeCommandSetNativeDesfire(isoDep, commandSet, logCommands);
        } else {
            return executeCommandSetIso7816(isoDep, commandSet, logCommands);
        }
    }

    public ReceiveSchema executeCommandSetNativeDesfire(IsoDep isoDep, TransmitSchema commandSet, boolean logCommands) throws IOException {
        ReceiveSchema rsp = newReceiveSchema(commandSet);

        List<Result> result = rsp.getResult();

        commands:
        for (Command command : commandSet.getCommand()) {

            String frame = command.getFrame();
            if (logCommands) {
                Log.d(LOG_TAG, "Transceive command " + command.getCommandId() + " : " + frame);
            }

            byte[] frameAsBytes = ByteArrayHexStringConverter.hexStringToByteArray(frame.substring(2));

            byte[] nativeResponseBytes;
            if (handleAF(command)) {
                nativeResponseBytes = processHandleAfFrameNativeDesfire(isoDep, frameAsBytes);
            } else {
                nativeResponseBytes = isoDep.transceive(frameAsBytes);
            }

            if (logCommands) {
                Log.d(LOG_TAG, "Transceive response: " + command.getCommandId() + " : " + ByteArrayHexStringConverter.toHexString(nativeResponseBytes));
            }

            Result r = new Result();
            r.setCommandId(command.getCommandId());
            r.setFrame("0x" + ByteArrayHexStringConverter.toHexString(nativeResponseBytes));

            result.add(r);

            // is status is not as expected, discontinue
            String expStatus = command.getExpStatus();
            if (expStatus != null && !expStatus.isEmpty()) {
                byte[] expectedStatusAsBytes = ByteArrayHexStringConverter.hexStringToByteArray(expStatus.substring(2));

                if(expectedStatusAsBytes.length > nativeResponseBytes.length) {
                    break;
                }

                // desfire native command status is seemingly in the start of the response bytes
                for(int i = 0; i < expectedStatusAsBytes.length; i++) {
                    if(expectedStatusAsBytes[i] != nativeResponseBytes[i]) {
                        break;
                    }
                }

            }
        }

        return rsp;
    }

    private ReceiveSchema newReceiveSchema(TransmitSchema commandSet) {
        ReceiveSchema result = new ReceiveSchema();

        if(commandSet.getApduType() == TransmitSchema.ApduType.DESFIRE) {
            result.setApduType(ReceiveSchema.ApduType.DESFIRE);
        } else {
            result.setApduType(ReceiveSchema.ApduType.ISO_7816);
        }
        result.setDeviceId(commandSet.getDeviceId());
        result.setTraceId(commandSet.getTraceId());
        result.setTransceiveId(commandSet.getTransceiveId());
        result.setEventTimestamp(new Date());

        return result;
    }

    public ReceiveSchema executeCommandSetIso7816(IsoDep isoDep, TransmitSchema commandSet, boolean logCommands) throws IOException {
        ReceiveSchema rsp = newReceiveSchema(commandSet);

        List<Result> result = rsp.getResult();

        commands:
        for (Command command : commandSet.getCommand()) {

            String frame = command.getFrame();
            if (logCommands) {
                Log.d(LOG_TAG, "Transceive command " + command.getCommandId() + " : " + frame);
            }

            byte[] frameAsBytes = ByteArrayHexStringConverter.hexStringToByteArray(frame.substring(2));

            byte[] nativeResponseBytes;
            if (handleAF(command)) {
                nativeResponseBytes = processHandleAfFrameIso7816(isoDep, frameAsBytes);
            } else {
                nativeResponseBytes = isoDep.transceive(frameAsBytes);
            }

            if (logCommands) {
                Log.d(LOG_TAG, "Transceive response: " + command.getCommandId() + " : " + ByteArrayHexStringConverter.toHexString(nativeResponseBytes));
            }

            Result r = new Result();
            r.setCommandId(command.getCommandId());
            r.setFrame("0x" + ByteArrayHexStringConverter.toHexString(nativeResponseBytes));

            result.add(r);

            // is status is not as expected, discontinue
            String expStatus = command.getExpStatus();
            if (expStatus != null && !expStatus.isEmpty()) {
                byte[] expectedStatusAsBytes = ByteArrayHexStringConverter.hexStringToByteArray(expStatus.substring(2));

                if(expectedStatusAsBytes.length > nativeResponseBytes.length) {
                    break;
                }

                // status is in the end of response bytes (sw1, sw2)
                // TODO this does not 100% align with the description in
                // https://github.com/entur/hwb/blob/main/specifications/validators/nfc/apdu/%5BdeviceId%5D/transmit/transmit.schema.json#L55
                int offset = nativeResponseBytes.length - expectedStatusAsBytes.length;
                for(int i = 0; i < expectedStatusAsBytes.length; i++) {
                    if(expectedStatusAsBytes[i] != nativeResponseBytes[offset + i]) {
                        break;
                    }
                }

            }
        }

        return rsp;
	}

    private static boolean handleAF(Command command) {
        return command.getExpStatus() != null && command.getExpStatus().equals("0x00");
    }

    /**
	 * Process a handleAF=true apduFrame command.
	 * <p>
	 * Response from card consist of multiple frames (if status = AF). Fetch more frames by sending 'AF' command to card until response no longer has
	 * status='AF'. Combine all response frames into one and use only the status from the last response.
	 *
	 * The commands seem to be in Native Command mode, i.e. not wrapped in APDUs.
	 *
	 * https://stackoverflow.com/questions/40101316/whats-the-difference-between-desfire-and-desfire-ev1-cards
	 */

	@NonNull
	private byte[] processHandleAfFrameNativeDesfire(IsoDep isoDep, byte[] frame) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		output.write(0); // make room for final status
		do {
			byte[] result = isoDep.transceive(frame);

			// append payload
			output.write(result, 1, result.length - 1);

			byte status = result[0];
			if (status != STATUS_ADDITIONAL_FRAME) {
				// finished
				byte[] finalResult = output.toByteArray();

				// copy status from the final transceive result
				finalResult[0] = status;

				return finalResult;
			}

			// at least one more frame
			frame = ADDITIONAL_FRAME_NATIVE_DESFIRE;
		} while (true);
	}

    @NonNull
    private byte[] processHandleAfFrameIso7816(IsoDep isoDep, byte[] frame) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        do {
            byte[] response = isoDep.transceive(frame);

            if (response.length >= 2 && response[response.length - 2] == STATUS_OK && response[response.length - 1] == STATUS_ADDITIONAL_FRAME) {
                // append payload
                output.write(response, 2, response.length - 2);

                // at least one more frame
                frame = ADDITIONAL_FRAME_ISO7816;

                continue;
            }

            output.write(response);

            return output.toByteArray();
        } while (true);
    }
}
