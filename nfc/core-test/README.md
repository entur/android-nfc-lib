# core-test
This library helps with testing interaction with `Tag` via simulated `IsoDep` and `MifareUltralight` ADPUs. 

example use:
```
// mock Mifare Desfire EV1:

MockTag mockTag = MockTag.newBuilder()
        .withContext(activity)
        .withTagId(new byte[]{0x04, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06})
        .withIsoDep( (isoDep) -> {
            isoDep.withDesfireEV1(); // desfire
            isoDep.withTransceive(ListMockTransceive.newBuilder()
                    .withErrorResponse("63") // raw desfire response
                    .withTransceive("5A008057", "00") // raw desfire command
                    .build());
            }
        )
        .build();

// publish intent
mockTag.power();

```
