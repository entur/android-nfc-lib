package no.entur.android.nfc.external.service.tag;

import java.util.ArrayList;
import java.util.List;

public class TagProxyStore {

	protected static final String TAG = TagProxyStore.class.getName();

	private static int counter = 1;

	public static int nextServiceHandle() {
		synchronized (TagProxyStore.class) {
			counter++;

			return counter;
		}
	}

	private List<TagProxy> items = new ArrayList<TagProxy>();

	public List<TagProxy> getItems() {
		return items;
	}

	public void setItems(List<TagProxy> items) {
		this.items = items;
	}

	public TagProxy add(int slotNumber, List<TagTechnology> technologies) {
		int next = nextServiceHandle();

		TagProxy tagProxy = new TagProxy(next, slotNumber, technologies);

		add(tagProxy);

		return tagProxy;
	}

	public boolean add(TagProxy object) {
		synchronized (items) {
			return items.add(object);
		}
	}

	public boolean remove(TagProxy proxy) {
		proxy.setPresent(false);
		synchronized (items) {
			return items.remove(proxy);
		}
	}

	public void removeItem(int slotNumber) {
		synchronized (items) {
			for (TagProxy tagItem : items) {
				if (tagItem.getSlotNumber() == slotNumber) {
					tagItem.setPresent(false);

					items.remove(tagItem);

					return;
				}
			}
		}

	}

	public TagProxy get(int serviceHandle) {
		// Log.d(TAG, "Get service handle " + serviceHandle);
		synchronized (items) {
			for (TagProxy tagItem : items) {
				if (tagItem.getHandle() == serviceHandle) {
					return tagItem;
				}
			}
		}
		return null;
	}

}
