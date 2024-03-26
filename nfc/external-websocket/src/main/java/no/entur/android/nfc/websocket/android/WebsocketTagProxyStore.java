package no.entur.android.nfc.websocket.android;

import java.util.ArrayList;
import java.util.List;

import no.entur.android.nfc.external.service.tag.TagProxy;
import no.entur.android.nfc.external.service.tag.TagProxyStore;
import no.entur.android.nfc.external.service.tag.TagTechnology;

public class WebsocketTagProxyStore implements TagProxyStore {

	protected static final String TAG = WebsocketTagProxyStore.class.getName();

	private static int counter = 1;

	public static int nextServiceHandle() {
		synchronized (WebsocketTagProxyStore.class) {
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

		TagProxy tagProxy = new WebsocketTagProxy(next, slotNumber, technologies, this);

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

	public void removeItem(int slotNumber) {
		synchronized (items) {
			for (TagProxy tagItem : items) {
				if (tagItem.getSlotNumber() == slotNumber) {
					tagItem.setPresent(false);
					tagItem.closeTechnology();

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
