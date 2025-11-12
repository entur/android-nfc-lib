# core-test
This library helps with testing interaction with `Tag` via simulated `IsoDep` and `MifareUltralight` ADPUs. 

Example of use below.

Mifare Desfire EV1:
```
MockTag mockTag = MockTag.newBuilder()
        .withRandomTagId()
        .withIsoDep( (isoDep) -> {
            isoDep.withDesfireEV1(); // desfire
            isoDep.withTransceive(ListMockTransceive.newBuilder()
                    .withErrorResponse("63") // raw desfire response
                    .withTransceiveNativeDesfireSelectApplication("008057", "00") // raw desfire command
                    .build());
            }
        )
        .build();

MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

mockExternalReader.open();

mockExternalReader.tagEnteredField(mockTag);
```

Mifare Ultralight tag:

```
MockTag mockTag = MockTag.newBuilder()
        .withMifareUltralight( (ul) -> {
            ul.withMemoryLayout((mem) -> {
               mem.withPage(3, new byte[]{0x00, 0x01, 0x02, 0x03});
            });
        })
        .build();

MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

mockExternalReader.open();

mockExternalReader.tagEnteredField(mockTag);
```