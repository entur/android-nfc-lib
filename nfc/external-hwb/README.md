# external-hwb
Support for [HWB](https://github.com/entur/hwb/tree/main) messages over MQTT.

Topics:

## `validators/nfc`
Card present. Optionally contains a card dump or token id.

## validators/nfc/apdu/[deviceId]/transmit
Send APDU (or collection of ADPU) to specific device.

## `validators/nfc/apdu/receive`
Response for `validators/nfc/apdu/[deviceId]/transmit` (from all readers)

## `/device/diagnostics/request`
Request status for all readers (i.e. detect which readers are present).

## `/device/diagnostic`
Response for reader statuses.

## `/device/[deviceId]/diagnostics/request`
Requets specific reader status.

## `/device/[deviceId]/diagnostics`
Response for specific reader status.


