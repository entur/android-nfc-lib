package no.entur.android.nfc.detect.ats;

public class AnswerToResetAnalyzeResult implements Comparable<AnswerToResetAnalyzeResult> {

    private final String tagType;

    public AnswerToResetAnalyzeResult(String tagType) {
        this.tagType = tagType;
    }

    public String getTagType() {
        return tagType;
    }

    @Override
    public int compareTo(AnswerToResetAnalyzeResult o) {

        if(o.tagType == null && tagType == null) {
            return 0;
        }

        if(tagType != null && o.tagType == null) {
            // this is better
            return -1;
        }
        if(tagType == null && o.tagType != null) {
            // other is better
            return 1;
        }

        return 0;
    }
}
