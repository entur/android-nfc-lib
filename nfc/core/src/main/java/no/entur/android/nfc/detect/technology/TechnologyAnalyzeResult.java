package no.entur.android.nfc.detect.technology;


public class TechnologyAnalyzeResult implements Comparable<TechnologyAnalyzeResult> {

    private final String type;

    public TechnologyAnalyzeResult(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(TechnologyAnalyzeResult o) {

        if(o.type == null && type == null) {
            return 0;
        }

        if(type != null && o.type == null) {
            // this is better
            return -1;
        }
        if(type == null && o.type != null) {
            // other is better
            return 1;
        }

        return 0;
    }
}
