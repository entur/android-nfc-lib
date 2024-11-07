package no.entur.android.nfc.detect.uid;

import java.util.Comparator;
import java.util.Objects;

// https://gototags.com/nfc/chip/features/uid
public class UidAnalyzeResult implements Comparable<UidAnalyzeResult> {

    private static final Comparator<UidSequenceType> SEQUENCE_TYPE_COMPARATOR = new Comparator<UidSequenceType>() {
        @Override
        public int compare(UidSequenceType o1, UidSequenceType o2) {
            if(o1 == o2) {
                return 0;
            }

            // inside is best
            if(o1 != UidSequenceType.MATCH && o2 == UidSequenceType.MATCH) {
                return 1;
            } else if(o1 == UidSequenceType.MATCH && o2 != UidSequenceType.MATCH) {
                return -1;
            }

            // better to have no sequence info than to be outside
            if(o1 != UidSequenceType.NOT_AVAILABLE && o2 == UidSequenceType.NOT_AVAILABLE) {
                return 1;
            } else if(o1 == UidSequenceType.NOT_AVAILABLE && o2 != UidSequenceType.NOT_AVAILABLE) {
                return -1;
            }

            return 0;
        }
    };

    private static final Comparator<UidManufacturerType> UID_MANUFACTURER_TYPE_COMPARATOR = new Comparator<UidManufacturerType>() {
        @Override
        public int compare(UidManufacturerType o1, UidManufacturerType o2) {
            if(o1 == o2) {
                return 0;
            }

            // known is best
            if(o1 != UidManufacturerType.MATCH && o2 == UidManufacturerType.MATCH) {
                return 1;
            } else if(o1 == UidManufacturerType.MATCH && o2 != UidManufacturerType.MATCH) {
                return -1;
            }

            // better to have unknown manufacturer than to be outside
            if(o1 != UidManufacturerType.NOT_AVAILABLE && o2 == UidManufacturerType.NOT_AVAILABLE) {
                return 1;
            } else if(o1 == UidManufacturerType.NOT_AVAILABLE && o2 != UidManufacturerType.NOT_AVAILABLE) {
                return -1;
            }

            return 0;
        }
    };


    // matches length
    private final boolean length;

    // matches known range
    private final UidSequenceType sequenceType;

    private final UidManufacturerType manufacturerType;

    public UidAnalyzeResult(boolean length, UidSequenceType sequenceType, UidManufacturerType manufacturerType) {
        this.length = length;
        this.sequenceType = sequenceType;
        this.manufacturerType = manufacturerType;
    }

    public boolean isLength() {
        return length;
    }

    public UidManufacturerType getManufacturerType() {
        return manufacturerType;
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

        int manufacturer = UID_MANUFACTURER_TYPE_COMPARATOR.compare(manufacturerType, o.manufacturerType);
        if(manufacturer != 0) {
            return manufacturer;
        }

        return SEQUENCE_TYPE_COMPARATOR.compare(sequenceType, o.sequenceType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UidAnalyzeResult that = (UidAnalyzeResult) o;
        return length == that.length && sequenceType == that.sequenceType && manufacturerType == that.manufacturerType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, sequenceType, manufacturerType);
    }
}
