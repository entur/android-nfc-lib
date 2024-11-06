package no.entur.android.nfc.detect.tag;

import java.util.Comparator;

// https://gototags.com/nfc/chip/features/uid
public class UidAnalyzeResult implements Comparable<UidAnalyzeResult> {

    private static final Comparator<UidSequenceType> SEQUENCE_TYPE_COMPARATOR = new Comparator<UidSequenceType>() {
        @Override
        public int compare(UidSequenceType o1, UidSequenceType o2) {
            if(o1 == o2) {
                return 0;
            }

            // inside is best
            if(o1 != UidSequenceType.INSIDE && o2 == UidSequenceType.INSIDE) {
                return 1;
            } else if(o1 == UidSequenceType.INSIDE && o2 != UidSequenceType.INSIDE) {
                return -1;
            }

            // better to have no sequence info than to be outside
            if(o1 != UidSequenceType.UNKNOWN && o2 == UidSequenceType.UNKNOWN) {
                return 1;
            } else if(o1 == UidSequenceType.UNKNOWN && o2 != UidSequenceType.UNKNOWN) {
                return -1;
            }

            return 0;
        }
    };

    // matches length
    private final boolean length;

    // matches known range
    private final UidSequenceType sequenceType;

    private final boolean manufacturer;

    public UidAnalyzeResult(boolean length, UidSequenceType sequenceType, boolean manufacturer) {
        this.length = length;
        this.sequenceType = sequenceType;
        this.manufacturer = manufacturer;
    }

    public boolean isLength() {
        return length;
    }

    public boolean isManufacturer() {
        return manufacturer;
    }

    public UidSequenceType getSequenceType() {
        return sequenceType;
    }

    @Override
    public int compareTo(UidAnalyzeResult o) {
        // Compares this object with the specified object for order.
        // Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.

        if(!length && o.length) {
            return 1;
        } else if(length && !o.length) {
            return -1;
        }

        if(!manufacturer && o.manufacturer) {
            return 1;
        } else if(manufacturer && !o.manufacturer) {
            return -1;
        }

        return SEQUENCE_TYPE_COMPARATOR.compare(sequenceType, o.sequenceType);
    }
}
