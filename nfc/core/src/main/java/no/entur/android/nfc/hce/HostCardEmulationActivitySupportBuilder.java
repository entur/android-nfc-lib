package no.entur.android.nfc.hce;

import android.app.Activity;
import android.nfc.cardemulation.HostApduService;

public class HostCardEmulationActivitySupportBuilder {

	protected Activity activity;
	protected Class<? extends HostApduService> serviceClass;

	public HostCardEmulationActivitySupportBuilder withActivity(Activity activity) {
		this.activity = activity;
		return this;
	}

	public HostCardEmulationActivitySupportBuilder withService(Class<? extends HostApduService> serviceClass) {
		this.serviceClass = serviceClass;
		return this;
	}

	public HostCardEmulationActivitySupport build() {
		if (serviceClass == null) {
			throw new IllegalArgumentException("Expected service class");
		}
		if (activity == null) {
			throw new IllegalArgumentException("Expected activity");
		}

		return new HostCardEmulationActivitySupport(activity, serviceClass);
	}

}
