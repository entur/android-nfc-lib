package no.entur.android.nfc.detect.tag;

public class TagIdAnalyzeResult implements Comparable<TagIdAnalyzeResult> {

    // matches length
    private final boolean length;

    // matches known range
    private final boolean range;

    public TagIdAnalyzeResult(boolean length, boolean range) {
        this.length = length;
        this.range = range;
    }

    public boolean isLength() {
        return length;
    }

    public boolean isRange() {
        return range;
    }

    @Override
    public int compareTo(TagIdAnalyzeResult o) {
        // Compares this object with the specified object for order.
        // Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.

        if(range == o.range) {

            if(length == o.length) {
                return 0;
            }

            if(!length && o.length) {
                return 1;
            }
            return -1;

        } else if(!range && o.range) {
            return 1;
        }
        return -1;
    }
}
