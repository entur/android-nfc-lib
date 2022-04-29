# wrapper
Module cloning + wrapping black- and greylisted [Android NFC](https://github.com/aosp-mirror/platform_frameworks_base/tree/master/core/java/android/nfc) (android.nfc) classes.

Features:

 * Clones a lot of NFC-related interfaces (aidls) and classes, like Ndef, Tag and IsoDep
 * Adds wrappers so that the cloned source works in parallel with the native sources
 
Advantages:

 * external readers can use the same pattern as internal NFC (i.e. parcelable objects reconnecting to services on deserialization).
 * working with dual NFC stacks is simplified, even on the latest Android; independent of which classes will be grey- or blacklisted later.
 
Disadvantages:

 * increased footprint due to additonal (duplicated) classes
 * does not work out of the box with third party libraries - they must be modified / recompiled to work. This can be solved with a custom processor, but this is yet to be implemented.
 

