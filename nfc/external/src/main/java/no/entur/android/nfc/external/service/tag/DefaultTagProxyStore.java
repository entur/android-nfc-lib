package no.entur.android.nfc.external.service.tag;

import java.util.ArrayList;
import java.util.List;

public class DefaultTagProxyStore implements TagProxyStore {

	protected static final String TAG = DefaultTagProxyStore.class.getName();

	private static int counter = 1;

	public static int nextServiceHandle() {
		synchronized (DefaultTagProxyStore.class) {
			counter++;

			return counter;
		}
	}

	private List<TagProxy> items = new ArrayList<>();

	public List<TagProxy> getItems() {
		return items;
	}

	public void setItems(List<TagProxy> items) {
		this.items = items;
	}

	public TagProxy add(int slotNumber, List<TagTechnology> technologies) {
		int next = nextServiceHandle();

		TagProxy tagProxy = new DefaultTagProxy(next, slotNumber, technologies, this);

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
		proxy.closeTechnology();
		synchronized (items) {
			return items.remove(proxy);
		}
	}

	public TagProxy removeItem(int slotNumber) {
		synchronized (items) {
			for (TagProxy item : items) {
				if (item.getSlotNumber() == slotNumber) {
					item.setPresent(false);
					item.closeTechnology();

					items.remove(item);

					return item;
				}
			}
		}
        return null;
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
