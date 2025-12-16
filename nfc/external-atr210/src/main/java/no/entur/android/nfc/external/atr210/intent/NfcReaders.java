package no.entur.android.nfc.external.atr210.intent;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class NfcReaders implements Parcelable {

    private List<NfcTagReader> tagReaders = new ArrayList<>();

    private List<NfcSamReader> samReaders = new ArrayList<>();

    public List<NfcTagReader> getTagReaders() {
        return tagReaders;
    }

    public void setTagReaders(List<NfcTagReader> tagReaders) {
        this.tagReaders = tagReaders;
    }

    public List<NfcSamReader> getSamReaders() {
        return samReaders;
    }

    public void setSamReaders(List<NfcSamReader> samReaders) {
        this.samReaders = samReaders;
    }

    public void add(NfcSamReader r) {
        this.samReaders.add(r);
    }

    public void add(NfcTagReader r) {
        this.tagReaders.add(r);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tagReaders.size());
        for (NfcTagReader hfReader : tagReaders) {
            dest.writeString(hfReader.getId());
            dest.writeString(hfReader.getName());

            List<NfcCardStatus> status = hfReader.getStatus();
            dest.writeInt(status.size());
            for (NfcCardStatus s : status) {
                dest.writeInt(s.ordinal());
            }
        }

        dest.writeInt(samReaders.size());
        for (NfcSamReader samReader : samReaders) {
            dest.writeString(samReader.getId());
            dest.writeString(samReader.getName());

            List<NfcCardStatus> status = samReader.getStatus();
            dest.writeInt(status.size());
            for (NfcCardStatus s : status) {
                dest.writeInt(s.ordinal());
            }
        }

    }

    public static final Parcelable.Creator<NfcReaders> CREATOR = new Parcelable.Creator<NfcReaders>() {
        @Override
        public NfcReaders createFromParcel(Parcel in) {
            List<NfcTagReader> hfReaders = new ArrayList<>();

            int hfReadersSize = in.readInt();
            for(int i = 0; i < hfReadersSize; i++) {

                String id = in.readString();
                String name = in.readString();

                List<NfcCardStatus> status = new ArrayList<>();

                int statusCount = in.readInt();
                for(int k = 0; k < statusCount; k++) {
                    status.add(NfcCardStatus.fromOrdinal(in.readInt()));
                }

                hfReaders.add(new NfcTagReader(id, status, name));
            }

            List<NfcSamReader> samReaders = new ArrayList<>();
            int samReadersSize = in.readInt();
            for(int i = 0; i < samReadersSize; i++) {
                String id = in.readString();
                String name = in.readString();

                List<NfcCardStatus> status = new ArrayList<>();

                int statusCount = in.readInt();
                for(int k = 0; k < statusCount; k++) {
                    status.add(NfcCardStatus.fromOrdinal(in.readInt()));
                }

                samReaders.add(new NfcSamReader(id, status, name));
            }

            NfcReaders nfcReaders = new NfcReaders();

            nfcReaders.setTagReaders(hfReaders);
            nfcReaders.setSamReaders(samReaders);

            return nfcReaders;
        }

        @Override
        public NfcReaders[] newArray(int size) {
            return new NfcReaders[size];
        }
    };

}
