# external-test
This library helps with testing interaction with `Tag` via simulated external readers. 

Example of use:

```
MockExternalReader mockExternalReader = MockExternalReader.newBuilder().withContext(activity).build();

mockExternalReader.open();

MockTag mockTag = ... // your code here

mockExternalReader.tagEnteredField(mockTag);
```
