package no.entur.android.nfc.wrapper;

import android.os.Parcel;

public class TagWrapper extends Tag {

	protected android.nfc.Tag delegate;

	public TagWrapper(android.nfc.Tag delegate) {
		this.delegate = delegate;
	}

	public byte[] getId() {
		return delegate.getId();
	}

	public String[] getTechList() {
		return delegate.getTechList();
	}

	public android.nfc.Tag getDelegate() {
		return delegate;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.delegate, flags);
	}

	protected TagWrapper(Parcel in) {
		this.delegate = in.readParcelable(android.nfc.Tag.class.getClassLoader());
	}

	public static final Creator<TagWrapper> CREATOR = new Creator<TagWrapper>() {
		@Override
		public TagWrapper createFromParcel(Parcel source) {
			return new TagWrapper(source);
		}

		@Override
		public TagWrapper[] newArray(int size) {
			return new TagWrapper[size];
		}
	};
}
