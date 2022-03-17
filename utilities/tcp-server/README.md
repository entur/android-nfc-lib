# whitelist
Utility for uploading a whitelist to abt-core. 

The whitelist can be created using testlab using the script `runTokenLibraryAndroidTestAllDevices.sh` and a few manual steps.

See `WhitelistApplication` class.

## Units with disfunctional key attestation

### Features overview

 * KEY_ATTESTATION_LEVEL_HARDWARE
   * Most devices >= 29
 * KEY_ATTESTATION_LEVEL_SOFTWARE
   * 30-50% of devices <= 28
 * KEY_ATTESTATION_LEVEL_NONE
   * a few (5%) devices <= 28

Known `KEY_ATTESTATION_LEVEL_NONE` devices:

 * Lenovo K520 level 26
 * Sony H8216 level 26
 * LGE Nexus 5 level 23
 * LGE LG-AS110 level 23
 * motorola Moto G Play level 23
 * Sony H9493 level 28
   * No longer in sale in Norway
 * Xiaomi Redmi 6A level 27
   * No longer in sale in Norway
 * samsung SM-G532G level 23

 * Verify certificate validity period
   * Generally fw devices
